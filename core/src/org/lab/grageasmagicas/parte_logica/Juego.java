package org.lab.grageasmagicas.parte_logica;

import org.lab.estructuras.Point;
import org.lab.grageasmagicas.parte_logica.patron_jugada_posible.Movimiento;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Bermudez Martin, Kurchan Ines, Marinelli Giuliano
 */
public class Juego extends Observable implements Runnable {

    private final Gragea[][] matrizGrageas;
    private int alto;
    private int ancho;
    private int velocidad;
    private int cantGragea;
    private int movimientos;
    private int movimientosTotales;
    private int puntajeGanar;
    private int puntaje;
    private int nivel;
    private int primerGrageaX;
    private int primerGrageaY;
    private int segundaGrageaX;
    private int segundaGrageaY;
    private int poderMovDiagonalUsos;
    private int poderMovDiagonalUsosTotales;
    private int modoJuego;
    private int tiempoJuego;
    private int dificultad;
    private boolean superGrageaActivada;
    private boolean poderMovDiagonalActivado;
    private boolean pausa;
    private boolean hayJugadas;
    private boolean huboCombo;
    private AtomicBoolean finJuego;
    private CopyOnWriteArrayList<Point> grageasCombinadas;
    private Comprobador[] comprobadorAlto;
    private Comprobador[] comprobadorAncho;
    private Eliminador[] eliminadores;
    private CyclicBarrier barrierComp;
    private CyclicBarrier barrierElim;
    private CyclicBarrier barrierEntrada;
    private PatronControlador controladorJugada;
    private CyclicBarrier barrierVerificarJugada;

    public Juego(int ancho, int alto, int velocidad, int cantGragea, int movimientos, int puntajeGanar,
                 int nivel, int poderMovDiagonalUsos, int modoJuego, int tiempoJuego, int dificultad,
                 AtomicBoolean finJuego) {
        this.ancho = ancho;
        this.alto = alto;
        this.velocidad = velocidad;
        this.movimientos = 0;
        this.movimientosTotales = movimientos;
        this.cantGragea = cantGragea;
        this.puntajeGanar = puntajeGanar;
        this.puntaje = 0;
        this.nivel = nivel;
        this.primerGrageaX = -1;
        this.primerGrageaY = -1;
        this.segundaGrageaX = -1;
        this.segundaGrageaY = -1;
        this.poderMovDiagonalUsos = 0;
        this.poderMovDiagonalUsosTotales = poderMovDiagonalUsos;
        this.tiempoJuego = tiempoJuego;
        this.modoJuego = modoJuego;
        this.dificultad = dificultad;
        this.superGrageaActivada = false;
        this.poderMovDiagonalActivado = false;
        this.finJuego = finJuego;
        this.hayJugadas = true;
        this.pausa = false;
        matrizGrageas = new Gragea[alto][ancho];
        comprobadorAlto = new Comprobador[alto];
        comprobadorAncho = new Comprobador[ancho];
        eliminadores = new Eliminador[ancho];
        grageasCombinadas = new CopyOnWriteArrayList();

        Random random = new Random();

        //crea las grageas con un tipo aleatorio y las agrega a la matriz
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                matrizGrageas[i][j] = new Gragea(random.nextInt(cantGragea));
            }
        }

        //cargarMatrizDefault(matrizGrageas);
        barrierVerificarJugada = new CyclicBarrier(2);
        controladorJugada = new PatronControlador(matrizGrageas, barrierVerificarJugada, finJuego);
        Thread tControlador = new Thread(controladorJugada);
        tControlador.start();
        barrierComp = new CyclicBarrier(alto + ancho + 1);

        //crea y lanza los comprobadores
        Thread comprobadorThread;
        for (int i = 0; i < alto; i++) {
            comprobadorAlto[i] = new ComprobadorAlto(matrizGrageas, i, grageasCombinadas, barrierComp, finJuego);
            comprobadorThread = new Thread(comprobadorAlto[i]);
            comprobadorThread.start();
        }

        for (int i = 0; i < ancho; i++) {
            comprobadorAncho[i] = new ComprobadorAncho(matrizGrageas, i, grageasCombinadas, barrierComp, finJuego);
            comprobadorThread = new Thread(comprobadorAncho[i]);
            comprobadorThread.start();
        }

        barrierElim = new CyclicBarrier(ancho + 1);

        //crea y lanza los eliminadores
        Thread eliminadorThread;
        for (int i = 0; i < ancho; i++) {
            eliminadores[i] = new Eliminador(matrizGrageas, i, grageasCombinadas, barrierElim, cantGragea, finJuego);
            eliminadorThread = new Thread(eliminadores[i]);
            eliminadorThread.start();
        }
    }

    @Override
    public void run() {
        try {
            if (modoJuego == 1) {
                final Timer temporizador = new Timer();
                temporizador.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (tiempoJuego > 0) {
                            tiempoJuego--;
                        } else {
                            temporizador.cancel();
                        }
                    }
                }, 1000, 1000);
            }

            //incialmente se realizan las combinaciones que hallan salido de forma aleatoria
            combinacionInicial();

            while (!finJuego.get() && !condicionFinJuego()) {
                System.out.println("\033[32mJuega\033[30m");
                System.out.println("\033[32mPuntaje: \033[30m" + puntaje + "\n");
                //Imprime el juego por consola
                System.out.println(toStringComb(matrizGrageas));

                sincronizar();

                limpiarPosGrageas();

                boolean sonAdy = false;
                do {
                    //Permite que el usuario pueda interactuar con la interfaz
                    barrierEntrada.await();
                    //espera a que se produzca algun evento de la interfaz grafica
                    barrierEntrada.await();

                    /*System.out.print("Gragea inicial XY: ");
                    String gi = TecladoIn.readLine();
                    System.out.print("Gragea final XY: ");
                    String gf = TecladoIn.readLine();
                    System.out.println();
                    primerGrageaX = Integer.parseInt(gi.substring(0, 1));
                    primerGrageaY = Integer.parseInt(gi.substring(1, 2));
                    segundaGrageaX = Integer.parseInt(gf.substring(0, 1));
                    segundaGrageaY = Integer.parseInt(gf.substring(1, 2));*/


                    //verificar si el movimiento de las grageas es adyacente para el caso en que no
                    //se utilice ningun poder
                    sonAdy = verificarAdyacentes(primerGrageaX, primerGrageaY, segundaGrageaX, segundaGrageaY);

                    /*
                    if (!sonAdy && !superGrageaActivada && !poderMovDiagonalActivado && !finJuego.get()) {
                        System.out.println("\033[31mMovimiento no válido\033[30m \n");
                    }*/

                    //mientras que la jugada no involucre un movimiento valido seguira pidiendola
                }
                while (!sonAdy && !superGrageaActivada && !poderMovDiagonalActivado && !finJuego.get() && !condicionFinJuego());
                if (!finJuego.get() && !condicionFinJuego()) {
                    if (!superGrageaActivada) {
                        //invertimos las grageas de lugar
                        intercambiarGrageas(primerGrageaX, primerGrageaY, segundaGrageaX, segundaGrageaY);

                        //despierta a los comprobadores
                        barrierComp.await();
                        //queda a la espera de que los comprobadores terminen
                        barrierComp.await();

                        calcularCombos();

                        //Imprime el juego por consola
                        System.out.println("\033[34mGrageas intercambiadas\033[30m");
                        System.out.println(toStringComb(matrizGrageas));

                        sincronizar();
                        /*System.out.println("Presione enter...");
                        TecladoIn.read();*/
                    } else {
                        comboSuperGragea();

                        //Imprime el juego por consola
                        System.out.println("\033[34mSuper gragea activada\033[30m");
                        System.out.println(toStringComb(matrizGrageas));

                        sincronizar();
                        /*System.out.println("Presione enter...");
                        TecladoIn.read();*/
                    }
                    //si las combinaciones de grageas esta vacia significa que el intercambio esta mal hecho
                    if (grageasCombinadas.isEmpty()) {
                        System.out.println("\033[31mIntercambio de grageas incorrecto\033[30m");
                        System.out.println();
                        intercambiarGrageas(primerGrageaX, primerGrageaY, segundaGrageaX, segundaGrageaY);
                    } else {
                        movimientos++;
                        if (poderMovDiagonalActivado && !superGrageaActivada) {
                            poderMovDiagonalUsos++;
                        }
                        //desactiva el flag de super gragea en caso de que haya sido activada una
                        superGrageaActivada = false;
                        //limpia las posiciones para intercambiar o para la super gragea
                        limpiarPosGrageas();
                        //elimina las combinaciones hasta que no quede ninguna
                        eliminarCombinaciones();
                        //si no hay moviento posible se resetean todas las grageas
                        verificarSiExisteMovimiento();
                    }

                    //si el juego es pausado se detendra aqui
                    if (pausa) {
                        dormir();
                    }
                }
            }
            if (!finJuego.get()) {
                resultadoJuego();

                finJuego.set(true);

                sincronizar();

                barrierEntrada.await();
                barrierEntrada.await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Juego.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private void limpiarPosGrageas() {
        primerGrageaX = -1;
        primerGrageaY = -1;
        segundaGrageaX = -1;
        segundaGrageaY = -1;
    }

    public void sincronizar() {
        setChanged();
        notifyObservers();
    }

    /**
     * Agrega el puntaje ganado segun las grageas que se combinaron.
     */
    public void calcularPuntaje() {
        puntaje += grageasCombinadas.size() * 10;
    }

    public boolean condicionFinJuego() {
        boolean condicion = false;
        switch (modoJuego) {
            case 0:
                condicion = (movimientos >= movimientosTotales) || (puntaje >= puntajeGanar);
                break;
            case 1:
                condicion = (tiempoJuego <= 0);
                break;
        }
        return condicion;
    }

    public void resultadoJuego() {
        switch (modoJuego) {
            case 0:
                System.out.println("Ya no te quedan movimientos!");
                if (puntaje < puntajeGanar) {
                    System.out.println("Perdiste!");
                    System.out.println("Puntaje logrado: \033[32m" + puntaje + "\033[30m");
                } else {
                    System.out.println("Ganaste!");
                    System.out.println("Puntaje logrado: \033[32m" + puntaje + "\033[30m");
                }
                break;
            case 1:
                System.out.println("Se acabo el tiempo!");
                if (puntaje < puntajeGanar) {
                    System.out.println("Perdiste!");
                    System.out.println("Puntaje logrado: \033[32m" + puntaje + "\033[30m");
                } else {
                    System.out.println("Ganaste!");
                    System.out.println("Puntaje logrado: \033[32m" + puntaje + "\033[30m");
                }
                break;
        }
    }

    /**
     * Agrega a grageasCombinadas las grageas correspondientes si existe un combo en la jugada.
     * Un combo es cuando una gragea esta en medio de dos combinaciones, es decir en una cruz.
     * Se realiza despues de cada comprobacion.
     */
    public void calcularCombos() {
        boolean encontro;
        List<Point> grageasDuplicadas = new ArrayList();
        for (int i = 0; i < grageasCombinadas.size(); i++) {
            Point grageaAct = grageasCombinadas.get(i);
            if (!grageasDuplicadas.contains(grageaAct)) {
                encontro = false;
                int j = 0;
                do {
                    if (j != i && grageaAct.equals(grageasCombinadas.get(j))) {
                        grageasDuplicadas.add(grageaAct);
                        encontro = true;
                    }
                    j++;
                }
                while (!encontro && j < grageasCombinadas.size());
                //cuando encuentra un combo setea la variable huboCombo en true una única vez por jugada.
                if (!huboCombo && encontro) {
                    huboCombo = true;
                }
            }
        }

        for (int i = 0; i < grageasDuplicadas.size(); i++) {
            for (int j = 0; j < matrizGrageas.length; j++) {
                Point puntoAct = new Point(grageasDuplicadas.get(i).x, j);
                if (!grageasCombinadas.contains(puntoAct) && matrizGrageas[grageasDuplicadas.get(i).x][j].getTipo() != 100) {
                    grageasCombinadas.add(puntoAct);
                }
            }
            for (int j = 0; j < matrizGrageas[0].length; j++) {
                Point puntoAct = new Point(j, grageasDuplicadas.get(i).y);
                if (!grageasCombinadas.contains(puntoAct) && matrizGrageas[j][grageasDuplicadas.get(i).y].getTipo() != 100) {
                    grageasCombinadas.add(puntoAct);
                }
            }
        }
    }


    public void comboSuperGragea() {
        Point superGrageaAct;
        Point puntoAct;
        List<Point> superGrageas = new ArrayList();
        superGrageas.add(new Point(primerGrageaX, primerGrageaY));
        for (int i = 0; i < superGrageas.size(); i++) {
            superGrageaAct = superGrageas.get(i);
            for (int j = superGrageaAct.y - 2; j <= superGrageaAct.y + 2; j++) {
                for (int k = superGrageaAct.x - 2; k <= superGrageaAct.x + 2; k++) {
                    if (j >= 0 && j < matrizGrageas.length) {
                        if (k >= 0 && k < matrizGrageas[0].length) {
                            puntoAct = new Point(k, j);
                            if (!grageasCombinadas.contains(puntoAct)) {
                                grageasCombinadas.add(puntoAct);
                                if (matrizGrageas[puntoAct.x][puntoAct.y].getTipo() == 100 && !superGrageas.contains(puntoAct)) {
                                    superGrageas.add(puntoAct);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void combinacionInicial() {
        try {
            //despierta a los comprobadores
            barrierComp.await();
            //queda a la espera de que los comprobadores terminen
            barrierComp.await();

            calcularCombos();

            System.out.println("\033[34mPrimer matriz\033[30m \n");
            //Imprime el juego por consola
            System.out.println(toStringComb(matrizGrageas));

            sincronizar();
            /*System.out.println("Presione enter...");
            TecladoIn.read();*/

            eliminarCombinaciones();

            verificarSiExisteMovimiento();

        } catch (InterruptedException ex) {
            Logger.getLogger(Juego.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void eliminarCombinaciones() {
        try {
            while (!grageasCombinadas.isEmpty()) {
                System.out.println("\033[34mCombinacion y eliminacion de grageas: \033[30m");
                System.out.println("\033[31mEliminadores: \033[30m");

                //despierta a los eliminadores
                barrierElim.await();
                //queda en espera de que los eliminadores terminen
                barrierElim.await();

                //calcula y agrega las combinaciones logradas al puntaje antes de vaciar el buffer
                calcularPuntaje();

                //limpia el buffer con las combinaciones de grageas que ya fueron
                //eliminadas por los eliminadores
                grageasCombinadas.clear();

                //despierta a los comprobadores
                barrierComp.await();
                //queda a la espera de que los comprobadores terminen
                barrierComp.await();

                calcularCombos();

                //Imprime el juego por consola
                System.out.println();
                System.out.println(toStringComb(matrizGrageas));

                sincronizar();
                /*System.out.println("Presione enter...");
                TecladoIn.read();*/
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void verificarSiExisteMovimiento() {
        try {
            //habilita a controladorJugada a verificar si existe jugada posible
            barrierVerificarJugada.await();
            //espera que el verificador termine
            barrierVerificarJugada.await();
            while (!hayJugadaRecta()) {
                //cuando el verificador termina revisa si existe jugada
                System.out.println("No queda ningún movimiento posible!");
                hayJugadas = false;

                sincronizar();

                barrierEntrada.await();
                barrierEntrada.await();

                hayJugadas = true;

                //llenar grageas combinadas con todas las grageas
                for (int i = 0; i < ancho; i++) {
                    for (int j = 0; j < alto; j++) {
                        grageasCombinadas.add(new Point(i, j));
                    }
                }
                //despierta a los eliminadores
                barrierElim.await();
                //queda en espera de que los eliminadores terminen
                barrierElim.await();

                sincronizar();

                //limpia el buffer con las combinaciones de grageas que ya fueron
                //eliminadas por los eliminadores
                grageasCombinadas.clear();

                //despierta a los comprobadores
                barrierComp.await();
                //queda a la espera de que los comprobadores terminen
                barrierComp.await();

                eliminarCombinaciones();

                //habilita a controladorJugada a verificar si existe jugada posible
                barrierVerificarJugada.await();
                //espera que el verificador termine
                barrierVerificarJugada.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    /**
     * Devuelve un string con la matriz del juego para imprimirla por pantalla.
     *
     * @param juego
     * @return String de la matriz.
     */
    public String toString(Gragea[][] juego) {
        int alto = juego.length;
        int ancho = juego[0].length;
        String res = "    ";
        for (int i = 0; i < alto; i++) {
            res += " " + i;
        }
        res += "\n    ";
        for (int i = 0; i < alto; i++) {
            res += "__";
        }
        res += "\n";
        for (int i = 0; i < alto; i++) {
            res += i + "  | ";
            for (int j = 0; j < ancho - 1; j++) {
                res += "\033[3" + (juego[i][j].getTipo() + 1) + "m" + juego[i][j].getTipo() + "\033[30m";
                res += ",";
            }
            res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + "m" + juego[i][ancho - 1].getTipo() + "\033[30m";
            res += "\n";
        }
        return res;
    }

    /**
     * Devuelve un string con la matriz del juego indicando las combinaciones que se encontraron
     * para imprimirla por pantalla.
     *
     * @param juego
     * @return String de la matriz.
     */
    public String toStringComb(Gragea[][] juego) {
        int alto = juego.length;
        int ancho = juego[0].length;
        String res = "    ";
        for (int i = 0; i < alto; i++) {
            res += " " + i;
        }
        res += "\n    ";
        for (int i = 0; i < alto; i++) {
            res += "__";
        }
        res += "\n";
        for (int i = 0; i < alto; i++) {
            res += i + "  | ";
            for (int j = 0; j < ancho - 1; j++) {
                if (grageasCombinadas.contains(new Point(i, j))) {
                    if (juego[i][j].getTipo() != 100) {
                        res += "\033[3" + (juego[i][j].getTipo() + 1) + ";40m" + juego[i][j].getTipo() + "\033[30m";
                    } else {
                        res += "\033[3" + (juego[i][j].getTipo() + 1) + ";40m" + "S" + "\033[30m";
                    }
                    res += "\033[30;40m" + "," + "\033[30m";
                } else {
                    if (juego[i][j].getTipo() != 100) {
                        res += "\033[3" + (juego[i][j].getTipo() + 1) + "m" + juego[i][j].getTipo() + "\033[30m";
                    } else {
                        res += "\033[3" + (juego[i][j].getTipo() + 1) + "m" + "S" + "\033[30m";
                    }
                    res += ",";
                }
            }
            if (grageasCombinadas.contains(new Point(i, (ancho - 1)))) {
                if (juego[i][ancho - 1].getTipo() != 100) {
                    res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + ";40m" + juego[i][ancho - 1].getTipo() + "\033[30m";
                } else {
                    res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + ";40m" + "S" + "\033[30m";
                }
            } else {
                if (juego[i][ancho - 1].getTipo() != 100) {
                    res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + "m" + juego[i][ancho - 1].getTipo() + "\033[30m";
                } else {
                    res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + "m" + "S" + "\033[30m";
                }
            }
            res += "\n";
        }
        return res;
    }

    /**
     * Devuelve un string con la matriz del juego indicando las combinaciones que se encontraron
     * para imprimirla por pantalla.
     *
     * @param juego
     * @return String de la matriz.
     */
    public String toStringComb2(Gragea[][] juego) {
        int alto = juego.length;
        int ancho = juego[0].length;
        String res = "    ";
        for (int i = 0; i < alto; i++) {
            res += " " + i;
        }
        res += "\n    ";
        for (int i = 0; i < alto; i++) {
            res += "__";
        }
        res += "\n";
        for (int i = 0; i < alto; i++) {
            res += i + "  | ";
            for (int j = 0; j < ancho - 1; j++) {
                if (grageasCombinadas.contains(new Point(i, j))) {
                    res += juego[i][j].getTipo();
                    res += ",";
                } else {
                    res += juego[i][j].getTipo();
                    res += ",";
                }
            }
            res += juego[i][ancho - 1].getTipo();
            res += "\n";
        }
        return res;
    }

    /**
     * Duerme al juego (simula pausar).
     *
     * @throws InterruptedException
     */
    synchronized public void dormir() throws InterruptedException {
        wait();
    }

    /**
     * Despierta al juego (simula despausar).
     */
    synchronized public void despertar() {
        notify();
    }

    /**
     * Termina el hilo, seteando finJuego en true.
     */
    public void terminar() {
        try {
            finJuego.set(true);
            barrierComp.await();
            barrierElim.await();
            barrierVerificarJugada.await();
            System.out.println("\033[34mFIN\033[30m \n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }


    /**
     * Modifica el tipo de las grageas de forma aleatoria y lo indica por
     * consola.
     */
    public void random() {
        System.out.println("\033[34mRANDOM\033[30m \n");
        Random random = new Random();
        for (int i = 0; i < matrizGrageas.length; i++) {
            for (int j = 0; j < matrizGrageas[0].length; j++) {
                matrizGrageas[i][j].setTipo((random.nextInt(cantGragea)));
            }
        }
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    /**
     * Sube la velocidad del juego si es posible (la velocidad aumenta cuanto
     * menor es).
     */
    public void subirVelocidad() {
        if (this.velocidad > 0) {
            this.velocidad--;
        }
    }

    /**
     * Baja la velocidad del juego (la velocidad disminuye cuanto mayor es).
     */
    public void bajarVelocidad() {
        if (this.velocidad < 9) {
            this.velocidad++;
        }
    }

    public boolean getPausa() {
        return pausa;
    }

    public void setPausa(boolean pausa) {
        this.pausa = pausa;
    }

    /**
     * Pausea o despausea y muestra el mensaje por consola.
     */
    public void modPausa() {
        if (!pausa) {
            System.out.println("\033[34mPAUSE\033[30m \n");
        } else {
            System.out.println("\033[34mUNPAUSE\033[30m \n");
        }
        this.pausa = !pausa;
    }

    public Gragea[][] getMatrizGrageas() {
        return matrizGrageas;
    }

    /**
     * @param gix
     * @param giy
     * @param gfx
     * @param gfy
     */
    public void intercambiarGrageas(int gix, int giy, int gfx, int gfy) {
        Gragea grageaAux = matrizGrageas[gix][giy];
        System.out.println("gix: " + gix);
        System.out.println("giy: " + giy);
        System.out.println("gfx: " + gfx);
        System.out.println("gfy: " + gfy);
        matrizGrageas[gix][giy] = matrizGrageas[gfx][gfy];
        matrizGrageas[gfx][gfy] = grageaAux;
    }

    /**
     * @param gix
     * @param giy
     * @param gfx
     * @param gfy
     */
    public void intercambiarTipoGrageas(int gix, int giy, int gfx, int gfy) {
        int tipoAux = matrizGrageas[gix][giy].getTipo();
        matrizGrageas[gix][giy].setTipo(matrizGrageas[gfx][gfy].getTipo());
        matrizGrageas[gfx][gfy].setTipo(tipoAux);
    }

    private void cargarMatrizDefault(Gragea[][] matrizGr) {
        matrizGr[0][0] = new Gragea(1);
        matrizGr[0][1] = new Gragea(0);
        matrizGr[0][2] = new Gragea(1);
        matrizGr[0][3] = new Gragea(1);
        matrizGr[0][4] = new Gragea(0);
        matrizGr[1][0] = new Gragea(3);
        matrizGr[1][1] = new Gragea(2);
        matrizGr[1][2] = new Gragea(0);
        matrizGr[1][3] = new Gragea(1);
        matrizGr[1][4] = new Gragea(0);
        matrizGr[2][0] = new Gragea(2);
        matrizGr[2][1] = new Gragea(3);
        matrizGr[2][2] = new Gragea(0);
        matrizGr[2][3] = new Gragea(0);
        matrizGr[2][4] = new Gragea(2);
        matrizGr[3][0] = new Gragea(2);
        matrizGr[3][1] = new Gragea(3);
        matrizGr[3][2] = new Gragea(3);
        matrizGr[3][3] = new Gragea(2);
        matrizGr[3][4] = new Gragea(1);
        matrizGr[4][0] = new Gragea(0);
        matrizGr[4][1] = new Gragea(1);
        matrizGr[4][2] = new Gragea(1);
        matrizGr[4][3] = new Gragea(2);
        matrizGr[4][4] = new Gragea(3);
    }

    /**
     * Verifica que el intercambio de grageas sea una jugada válida.
     *
     * @param gix
     * @param giy
     * @param gfx
     * @param gfy
     * @return
     */
    private boolean verificarAdyacentes(int gix, int giy, int gfx, int gfy) {
        boolean res;
        //logica del juego
        res = ((gix + 1 == gfx && giy == gfy) || (gix - 1 == gfx && giy == gfy) || (gix == gfx && giy + 1 == gfy)
                || (gix == gfx && giy - 1 == gfy));
        //limites de la matriz
        res = (res && (gix >= 0) && (gix <= matrizGrageas.length - 1) && (gfx >= 0) && (gfx <= matrizGrageas.length - 1)
                && (giy >= 0) && (giy <= matrizGrageas[0].length - 1) && (gfy >= 0) && (gfy <= matrizGrageas.length - 1));
        return res;
    }

    public CyclicBarrier getBarrierEntrada() {
        return barrierEntrada;
    }

    public void setBarrierEntrada(CyclicBarrier barrierEntrada) {
        this.barrierEntrada = barrierEntrada;
    }

    public void setIntercambioGrageas(int gix, int giy, int gfx, int gfy) {
        this.primerGrageaX = gix;
        this.primerGrageaY = giy;
        this.segundaGrageaX = gfx;
        this.segundaGrageaY = gfy;
    }

    public int getPrimerGrageaX() {
        return primerGrageaX;
    }

    public void setPrimerGrageaX(int primerGrageaX) {
        this.primerGrageaX = primerGrageaX;
    }

    public int getPrimerGrageaY() {
        return primerGrageaY;
    }

    public void setPrimerGrageaY(int primerGrageaY) {
        this.primerGrageaY = primerGrageaY;
    }

    public int getSegundaGrageaX() {
        return segundaGrageaX;
    }

    public void setSegundaGrageaX(int segundaGrageaX) {
        this.segundaGrageaX = segundaGrageaX;
    }

    public int getSegundaGrageaY() {
        return segundaGrageaY;
    }

    public void setSegundaGrageaY(int segundaGrageaY) {
        this.segundaGrageaY = segundaGrageaY;
    }

    public CopyOnWriteArrayList<Point> getGrageasCombinadas() {
        return grageasCombinadas;
    }

    public void setGrageasCombinadas(CopyOnWriteArrayList<Point> grageasCombinadas) {
        this.grageasCombinadas = grageasCombinadas;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    public int getPuntajeGanar() {
        return puntajeGanar;
    }

    public void setPuntajeGanar(int puntajeGanar) {
        this.puntajeGanar = puntajeGanar;
    }

    public boolean isFinJuego() {
        return finJuego.get();
    }

    public void setFinJuego(boolean finJuego) {
        this.finJuego.set(finJuego);
    }

    public CyclicBarrier getBarrierComp() {
        return barrierComp;
    }

    public CyclicBarrier getBarrierElim() {
        return barrierElim;
    }

    public PatronControlador getControladorJugada() {
        return controladorJugada;
    }

    public boolean isHayJugadas() {
        return hayJugadas;
    }

    public void setHayJugadas(boolean hayJugadas) {
        this.hayJugadas = hayJugadas;
    }

    public int getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(int movimientos) {
        this.movimientos = movimientos;
    }

    public int getMovimientosTotales() {
        return movimientosTotales;
    }

    public void setMovimientosTotales(int movimientosTotales) {
        this.movimientosTotales = movimientosTotales;
    }

    public boolean getHuboCombo() {
        return huboCombo;
    }

    public void setHuboCombo(boolean b) {
        huboCombo = b;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public boolean isSuperGrageaActivada() {
        return superGrageaActivada;
    }

    public void setSuperGrageaActivada(boolean superGrageaActivada) {
        this.superGrageaActivada = superGrageaActivada;
    }

    public boolean isPoderMovDiagonalActivado() {
        return poderMovDiagonalActivado;
    }

    public void setPoderMovDiagonalActivado(boolean poderMovDiagonalActivado) {
        this.poderMovDiagonalActivado = poderMovDiagonalActivado;
    }

    public int getPoderMovDiagonalUsos() {
        return poderMovDiagonalUsos;
    }

    public void setPoderMovDiagonalUsos(int poderMovDiagonalUsos) {
        this.poderMovDiagonalUsos = poderMovDiagonalUsos;
    }

    public int getPoderMovDiagonalUsosTotales() {
        return poderMovDiagonalUsosTotales;
    }

    public void setPoderMovDiagonalUsosTotales(int poderMovDiagonalUsosTotales) {
        this.poderMovDiagonalUsosTotales = poderMovDiagonalUsosTotales;
    }

    public int getModoJuego() {
        return modoJuego;
    }

    public int getTiempoJuego() {
        return tiempoJuego;
    }

    public int getDificultad() {
        return dificultad;
    }

    public void setDificultad(int dificultad) {
        this.dificultad = dificultad;
    }

    public Movimiento getJugadaRecta() {
        return controladorJugada.getJugadaRecta();
    }

    public boolean hayJugadaRecta() {
        return controladorJugada.existeJugadaRecta();
    }

    public boolean puedeUsarJugadaDiagonal() {
        return (poderMovDiagonalUsosTotales - poderMovDiagonalUsos != 0);
    }
}
