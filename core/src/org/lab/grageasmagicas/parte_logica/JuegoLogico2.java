//package org.lab.grageasmagicas.parte_logica;
//
//import org.lab.estructuras.Point;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Observable;
//import java.util.Random;
//import java.util.concurrent.BrokenBarrierException;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.CyclicBarrier;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//
///**
// * @author Bermudez Martin, Kurchan Ines, Marinelli Giuliano
// */
//public class JuegoLogico2 extends Observable implements Runnable {
//
//    private final Gragea[][] matrizGrageas;
//    private int alto;
//    private int ancho;
//    private int velocidad;
//    private int cantGragea;
//    private int movimientos;
//    private int movimientosTotales;
//    private int puntajeGanar;
//    private int puntaje;
//    private int primerGrageaX;
//    private int primerGrageaY;
//    private int segundaGrageaX;
//    private int segundaGrageaY;
//    private boolean pausa;
//    private boolean hayJugadas;
//    private boolean huboCombo;
//    private AtomicBoolean finJuego;
//    private CopyOnWriteArrayList<Point> grageasCombinadas;
//    private Comprobador[] comprobadorAlto;
//    private Comprobador[] comprobadorAncho;
//    private Eliminador[] eliminadores;
//    private CyclicBarrier barrierComp;
//    private CyclicBarrier barrierElim;
//    private CyclicBarrier barrierEntrada;
//    private PatronControlador controladorJugada;
//    private CyclicBarrier barrierVerificarJugada;
//
//
//    public JuegoLogico2(int ancho, int alto, int velocidad, int cantGragea, int movimientos, int puntajeGanar, AtomicBoolean finJuego) {
//        this.ancho = ancho;
//        this.alto = alto;
//        this.velocidad = velocidad;
//        this.movimientos = 0;
//        this.movimientosTotales = movimientos;
//        this.cantGragea = cantGragea;
//        this.puntajeGanar = puntajeGanar;
//        this.puntaje = 0;
//        this.primerGrageaX = -1;
//        this.primerGrageaY = -1;
//        this.segundaGrageaX = -1;
//        this.segundaGrageaY = -1;
//        this.finJuego = finJuego;
//        this.hayJugadas = true;
//        this.pausa = false;
//        matrizGrageas = new Gragea[alto][ancho];
//        comprobadorAlto = new Comprobador[alto];
//        comprobadorAncho = new Comprobador[ancho];
//        eliminadores = new Eliminador[ancho];
//        grageasCombinadas = new CopyOnWriteArrayList();
//
//        Random random = new Random();
//
//        //crea las grageas con un tipo aleatorio y las agrega a la matriz
//        for (int i = 0; i < alto; i++) {
//            for (int j = 0; j < ancho; j++) {
//                matrizGrageas[i][j] = new Gragea(random.nextInt(cantGragea));
//            }
//        }
//
//        //cargarMatrizDefault(matrizGrageas);
//        barrierVerificarJugada = new CyclicBarrier(2);
//        controladorJugada = new PatronControlador(matrizGrageas, barrierVerificarJugada, finJuego);
//        Thread tControlador = new Thread(controladorJugada);
//        tControlador.start();
//        barrierComp = new CyclicBarrier(alto + ancho + 1);
//
//        //crea y lanza los comprobadores
//        Thread comprobadorThread;
//        for (int i = 0; i < alto; i++) {
//            comprobadorAlto[i] = new ComprobadorAlto(matrizGrageas, i, grageasCombinadas, barrierComp, finJuego);
//            comprobadorThread = new Thread(comprobadorAlto[i]);
//            comprobadorThread.start();
//        }
//
//        for (int i = 0; i < ancho; i++) {
//            comprobadorAncho[i] = new ComprobadorAncho(matrizGrageas, i, grageasCombinadas, barrierComp, finJuego);
//            comprobadorThread = new Thread(comprobadorAncho[i]);
//            comprobadorThread.start();
//        }
//
//        barrierElim = new CyclicBarrier(ancho + 1);
//
//        //crea y lanza los eliminadores
//        Thread eliminadorThread;
//        for (int i = 0; i < ancho; i++) {
//            eliminadores[i] = new Eliminador(matrizGrageas, i, grageasCombinadas, barrierElim, cantGragea, finJuego);
//            eliminadorThread = new Thread(eliminadores[i]);
//            eliminadorThread.start();
//        }
//    }
//
//    @Override
//    public void run() {
//        try {
//            //incialmente se realizan las combinaciones que hayan salido de forma aleatoria
//            if (hayCombinadas()) {
//                comprobarCombinaciones();
//            }
//            while (!finJuego.get() && movimientos < movimientosTotales) {
//
//                System.out.println("\033[32mJuega\033[30m");
//                System.out.println("\033[32mPuntaje: \033[30m" + puntaje + "\n");
//                //Imprime el juego por consola
//                System.out.println(toStringComb(matrizGrageas));
//                sincronizar();
//                if (verificarSiExisteMovimiento()) {
//                    limpiarPosGrageas();
//                    //habilita interaccion con usuario
//                    habilitarInteraccionUsuario();
//                    //si el usuario realiza un movimiento válido se intercambian las grageas
//                    intercambiarGrageas(primerGrageaX, primerGrageaY, segundaGrageaX, segundaGrageaY);
//                    //Imprime el juego por consola
//                    System.out.println("\033[34mGrageas intercambiadas\033[30m");
//                    System.out.println(toStringComb(matrizGrageas));
//                    sincronizar();
//                    //si el intercambio de grageas produce una combinacion se procede a comprobar y
//                    //eliminar esas combinacion.
//                    if (hayCombinadas()) {
//                        movimientos++;
//                        limpiarPosGrageas();
//                        comprobarCombinaciones();
//                    } else {
//                        //si el intercambio de grageas no produce ninguna combinacion entonces se regresan
//                        //las grageas a su lugar original
//                        System.out.println("Combinación de grageas incorrecto.");
//                        intercambiarGrageas(primerGrageaX, primerGrageaY, segundaGrageaX, segundaGrageaY);
//                    }
//                } else {
//                    mezclarGrageas();
//                    sincronizar();
//                    if (hayCombinadas()) {
//                        comprobarCombinaciones();
//                    }
//                }
//            }
//            //Imprime el juego por consola
//            System.out.println(toStringComb(matrizGrageas));
//            sincronizar();
//            //si sale del while es porque se quedó sin movimientos o se terminó el juego por alguna otra razon como tiempo, puntaje, etc
//            if (!finJuego.get()) {
//                //se setea finJuego en true porque se terminó el juego por quedarse sin movimientos
//                finJuego.set(true);
//                System.out.println("Ya no te quedan movimientos!");
//                if (puntaje < puntajeGanar) {
//                    System.out.println("Perdiste!");
//                    System.out.println("Puntaje logrado: \033[32m" + puntaje + "\033[30m");
//                } else {
//                    System.out.println("Ganaste!");
//                    System.out.println("Puntaje logrado: \033[32m" + puntaje + "\033[30m");
//                }
//                sincronizar();
//                barrierEntrada.await();
//                barrierEntrada.await();
//                /* aca hay que llamar a
//                barrierComp.await();
//                barrierElim.await();
//                barrierVerificarJugada.await();
//                pero se hace desde la parte grafica cuando se llama a terminar.*/
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * comprueba las combinaciones que hay y llama al eliminador de combinaciones
//     */
//    public void comprobarCombinaciones() {
//        do {
//            calcularCombos();
//            //Imprime el juego por consola
//            System.out.println();
//            System.out.println(toStringComb(matrizGrageas));
//            sincronizar();
//            eliminarCombinaciones();
//        } while (hayCombinadas());
//    }
//
//    /**
//     * permite que el usuario interactue con la interfaz grafica
//     */
//    public void habilitarInteraccionUsuario() {
//        boolean sonAdy;
//        try {
//            do {
//                barrierEntrada.await();
//                barrierEntrada.await();
//                //verificar si el movimiento de las grageas es válido.
//                sonAdy = verificarAdyacentes(primerGrageaX, primerGrageaY, segundaGrageaX, segundaGrageaY);
//                if (!sonAdy && !finJuego.get()) {
//                    System.out.println("\033[31mMovimiento no válido\033[30m \n");
//                }
//                //mientras que la jugada no involucre posiciones adyacentes seguirá pidiendo los valores.
//            } while (!sonAdy && !finJuego.get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * retorna true si existen grageas combinadas
//     *
//     * @return
//     */
//    public boolean hayCombinadas() {
//        try {
//            barrierComp.await();
//            barrierComp.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//        return !grageasCombinadas.isEmpty();
//    }
//
//    public void mezclarGrageas() {
//        try {
//            barrierEntrada.await();
//            barrierEntrada.await();
//            //llenar grageas combinadas con todas las grageas
//            for (int i = 0; i < ancho; i++) {
//                for (int j = 0; j < alto; j++) {
//                    grageasCombinadas.add(new Point(i, j));
//                }
//            }
//            //despierta a los eliminadores
//            barrierElim.await();
//            //queda en espera de que los eliminadores terminen
//            barrierElim.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void limpiarPosGrageas() {
//        primerGrageaX = -1;
//        primerGrageaY = -1;
//        segundaGrageaX = -1;
//        segundaGrageaY = -1;
//    }
//
//    public void sincronizar() {
//        setChanged();
//        notifyObservers();
//    }
//
//    /**
//     * Agrega a grageasCombinadas las grageas correspondientes si existe un combo en la jugada.
//     * Un combo es cuando una gragea esta en medio de dos combinaciones, es decir en una cruz.
//     * Se realiza despues de cada comprobacion.
//     */
//    public void calcularCombos() {
//        boolean encontro = false;
//        List<Point> grageasDuplicadas = new ArrayList();
//        for (int i = 0; i < grageasCombinadas.size(); i++) {
//            Point grageaAct = grageasCombinadas.get(i);
//            if (!grageasDuplicadas.contains(grageaAct)) {
//                encontro = false;
//                int j = 0;
//                do {
//                    if (j != i && grageaAct.equals(grageasCombinadas.get(j))) {
//                        grageasDuplicadas.add(grageaAct);
//                        encontro = true;
//                    }
//                    j++;
//                } while (!encontro && j < grageasCombinadas.size());
//                //cuando encuentra un combo setea la variable huboCombo en true una única vez por jugada.
//                if (!huboCombo && encontro) {
//                    huboCombo = true;
//                }
//            }
//        }
//
//        for (int i = 0; i < grageasDuplicadas.size(); i++) {
//            for (int j = 0; j < matrizGrageas.length; j++) {
//                Point puntoAct = new Point(grageasDuplicadas.get(i).x, j);
//                if (!grageasCombinadas.contains(puntoAct)) {
//                    grageasCombinadas.add(puntoAct);
//                }
//            }
//            for (int j = 0; j < matrizGrageas[0].length; j++) {
//                Point puntoAct = new Point(j, grageasDuplicadas.get(i).y);
//                if (!grageasCombinadas.contains(puntoAct)) {
//                    grageasCombinadas.add(puntoAct);
//                }
//            }
//        }
//    }
//
//    /**
//     * Agrega el puntaje ganado segun las grageas que se combinaron.
//     */
//    public void calcularPuntaje() {
//        puntaje += grageasCombinadas.size() * 10;
//    }
//
//    public void combinacionInicial() {
//        try {
//            //despierta a los comprobadores
//            barrierComp.await();
//            //queda a la espera de que los comprobadores terminen
//            barrierComp.await();
//
//            //calcularCombos();
//
//            System.out.println("\033[34mPrimer matriz\033[30m \n");
//            //Imprime el juego por consola
//            System.out.println(toStringComb(matrizGrageas));
//
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Juego.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void eliminarCombinaciones() {
//        try {
//            System.out.println("\033[34mCombinacion y eliminacion de grageas: \033[30m");
//            System.out.println("\033[31mEliminadores: \033[30m");
//            //despierta a los eliminadores
//            barrierElim.await();
//            //queda en espera de que los eliminadores terminen
//            barrierElim.await();
//            calcularPuntaje();
//            grageasCombinadas.clear();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public boolean verificarSiExisteMovimiento() {
//        try {
//            //habilita a controladorJugada a verificar si existe jugada posible
//            barrierVerificarJugada.await();
//            //espera que el verificador termine
//            barrierVerificarJugada.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//        return controladorJugada.existeJugadaRecta();
//    }
//
//    /**
//     * Devuelve un string con la matriz del juego para imprimirla por pantalla.
//     *
//     * @param juego
//     * @return String de la matriz.
//     */
//    public String toString(Gragea[][] juego) {
//        int alto = juego.length;
//        int ancho = juego[0].length;
//        String res = "    ";
//        for (int i = 0; i < alto; i++) {
//            res += " " + i;
//        }
//        res += "\n    ";
//        for (int i = 0; i < alto; i++) {
//            res += "__";
//        }
//        res += "\n";
//        for (int i = 0; i < alto; i++) {
//            res += i + "  | ";
//            for (int j = 0; j < ancho - 1; j++) {
//                res += "\033[3" + (juego[i][j].getTipo() + 1) + "m" + juego[i][j].getTipo() + "\033[30m";
//                res += ",";
//            }
//            res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + "m" + juego[i][ancho - 1].getTipo() + "\033[30m";
//            res += "\n";
//        }
//        return res;
//    }
//
//    /**
//     * Devuelve un string con la matriz del juego indicando las combinaciones que se encontraron
//     * para imprimirla por pantalla.
//     *
//     * @param juego
//     * @return String de la matriz.
//     */
//    public String toStringComb(Gragea[][] juego) {
//        int alto = juego.length;
//        int ancho = juego[0].length;
//        String res = "    ";
//        for (int i = 0; i < alto; i++) {
//            res += " " + i;
//        }
//        res += "\n    ";
//        for (int i = 0; i < alto; i++) {
//            res += "__";
//        }
//        res += "\n";
//        for (int i = 0; i < alto; i++) {
//            res += i + "  | ";
//            for (int j = 0; j < ancho - 1; j++) {
//                if (grageasCombinadas.contains(new Point(i, j))) {
//                    res += "\033[3" + (juego[i][j].getTipo() + 1) + ";40m" + juego[i][j].getTipo() + "\033[30m";
//                    res += "\033[30;40m" + "," + "\033[30m";
//                } else {
//                    res += "\033[3" + (juego[i][j].getTipo() + 1) + "m" + juego[i][j].getTipo() + "\033[30m";
//                    res += ",";
//                }
//            }
//            if (grageasCombinadas.contains(new Point(i, (ancho - 1)))) {
//                res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + ";40m" + juego[i][ancho - 1].getTipo() + "\033[30m";
//            } else {
//                res += "\033[3" + (juego[i][ancho - 1].getTipo() + 1) + "m" + juego[i][ancho - 1].getTipo() + "\033[30m";
//            }
//            res += "\n";
//        }
//        return res;
//    }
//
//    /**
//     * Devuelve un string con la matriz del juego indicando las combinaciones que se encontraron
//     * para imprimirla por pantalla.
//     *
//     * @param juego
//     * @return String de la matriz.
//     */
//    public String toStringComb2(Gragea[][] juego) {
//        int alto = juego.length;
//        int ancho = juego[0].length;
//        String res = "    ";
//        for (int i = 0; i < alto; i++) {
//            res += " " + i;
//        }
//        res += "\n    ";
//        for (int i = 0; i < alto; i++) {
//            res += "__";
//        }
//        res += "\n";
//        for (int i = 0; i < alto; i++) {
//            res += i + "  | ";
//            for (int j = 0; j < ancho - 1; j++) {
//                if (grageasCombinadas.contains(new Point(i, j))) {
//                    res += juego[i][j].getTipo();
//                    res += ",";
//                } else {
//                    res += juego[i][j].getTipo();
//                    res += ",";
//                }
//            }
//            res += juego[i][ancho - 1].getTipo();
//            res += "\n";
//        }
//        return res;
//    }
//
//    /**
//     * Duerme al juego (simula pausar).
//     *
//     * @throws InterruptedException
//     */
//    synchronized public void dormir() throws InterruptedException {
//        wait();
//    }
//
//    /**
//     * Despierta al juego (simula despausar).
//     */
//    synchronized public void despertar() {
//        notify();
//    }
//
//    /**
//     * Termina el hilo, seteando finJuego en true.
//     */
//    public void terminar() {
//        try {
//            finJuego.set(true);
//            barrierComp.await();
//            barrierElim.await();
//            barrierVerificarJugada.await();
//            System.out.println("\033[34mFIN\033[30m \n");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (BrokenBarrierException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * Modifica el tipo de las grageas de forma aleatoria y lo indica por
//     * consola.
//     */
//    public void random() {
//        System.out.println("\033[34mRANDOM\033[30m \n");
//        Random random = new Random();
//        for (int i = 0; i < matrizGrageas.length; i++) {
//            for (int j = 0; j < matrizGrageas[0].length; j++) {
//                matrizGrageas[i][j].setTipo((random.nextInt(cantGragea)));
//            }
//        }
//    }
//
//    public int getVelocidad() {
//        return velocidad;
//    }
//
//    public void setVelocidad(int velocidad) {
//        this.velocidad = velocidad;
//    }
//
//    /**
//     * Sube la velocidad del juego si es posible (la velocidad aumenta cuanto
//     * menor es).
//     */
//    public void subirVelocidad() {
//        if (this.velocidad > 0) {
//            this.velocidad--;
//        }
//    }
//
//    /**
//     * Baja la velocidad del juego (la velocidad disminuye cuanto mayor es).
//     */
//    public void bajarVelocidad() {
//        if (this.velocidad < 9) {
//            this.velocidad++;
//        }
//    }
//
//    public boolean getPausa() {
//        return pausa;
//    }
//
//    public void setPausa(boolean pausa) {
//        this.pausa = pausa;
//    }
//
//    /**
//     * Pausea o despausea y muestra el mensaje por consola.
//     */
//    public void modPausa() {
//        if (!pausa) {
//            System.out.println("\033[34mPAUSE\033[30m \n");
//        } else {
//            System.out.println("\033[34mUNPAUSE\033[30m \n");
//        }
//        this.pausa = !pausa;
//    }
//
//    public Gragea[][] getMatrizGrageas() {
//        return matrizGrageas;
//    }
//
//    /**
//     * @param gix
//     * @param giy
//     * @param gfx
//     * @param gfy
//     */
//    public void intercambiarGrageas(int gix, int giy, int gfx, int gfy) {
//        Gragea grageaAux = matrizGrageas[gix][giy];
//        matrizGrageas[gix][giy] = matrizGrageas[gfx][gfy];
//        matrizGrageas[gfx][gfy] = grageaAux;
//    }
//
//    /**
//     * @param gix
//     * @param giy
//     * @param gfx
//     * @param gfy
//     */
//    public void intercambiarTipoGrageas(int gix, int giy, int gfx, int gfy) {
//        int tipoAux = matrizGrageas[gix][giy].getTipo();
//        matrizGrageas[gix][giy].setTipo(matrizGrageas[gfx][gfy].getTipo());
//        matrizGrageas[gfx][gfy].setTipo(tipoAux);
//    }
//
//    private void cargarMatrizDefault(Gragea[][] matrizGr) {
//        matrizGr[0][0] = new Gragea(1);
//        matrizGr[0][1] = new Gragea(0);
//        matrizGr[0][2] = new Gragea(1);
//        matrizGr[0][3] = new Gragea(1);
//        matrizGr[0][4] = new Gragea(0);
//        matrizGr[1][0] = new Gragea(3);
//        matrizGr[1][1] = new Gragea(2);
//        matrizGr[1][2] = new Gragea(0);
//        matrizGr[1][3] = new Gragea(1);
//        matrizGr[1][4] = new Gragea(0);
//        matrizGr[2][0] = new Gragea(2);
//        matrizGr[2][1] = new Gragea(3);
//        matrizGr[2][2] = new Gragea(0);
//        matrizGr[2][3] = new Gragea(0);
//        matrizGr[2][4] = new Gragea(2);
//        matrizGr[3][0] = new Gragea(2);
//        matrizGr[3][1] = new Gragea(3);
//        matrizGr[3][2] = new Gragea(3);
//        matrizGr[3][3] = new Gragea(2);
//        matrizGr[3][4] = new Gragea(1);
//        matrizGr[4][0] = new Gragea(0);
//        matrizGr[4][1] = new Gragea(1);
//        matrizGr[4][2] = new Gragea(1);
//        matrizGr[4][3] = new Gragea(2);
//        matrizGr[4][4] = new Gragea(3);
//    }
//
//    /**
//     * Verifica que el intercambio de grageas sea una jugada válida.
//     *
//     * @param gix
//     * @param giy
//     * @param gfx
//     * @param gfy
//     * @return
//     */
//    private boolean verificarAdyacentes(int gix, int giy, int gfx, int gfy) {
//        boolean res;
//        //logica del juego
//        res = ((gix + 1 == gfx && giy == gfy) || (gix - 1 == gfx && giy == gfy) || (gix == gfx && giy + 1 == gfy)
//                || (gix == gfx && giy - 1 == gfy));
//        //limites de la matriz
//        res = (res && (gix >= 0) && (gix <= matrizGrageas.length - 1) && (gfx >= 0) && (gfx <= matrizGrageas.length - 1)
//                && (giy >= 0) && (giy <= matrizGrageas[0].length - 1) && (gfy >= 0) && (gfy <= matrizGrageas.length - 1));
//        return res;
//    }
//
//    public CyclicBarrier getBarrierEntrada() {
//        return barrierEntrada;
//    }
//
//    public void setBarrierEntrada(CyclicBarrier barrierEntrada) {
//        this.barrierEntrada = barrierEntrada;
//    }
//
//    public void setIntercambioGrageas(int gix, int giy, int gfx, int gfy) {
//        this.primerGrageaX = gix;
//        this.primerGrageaY = giy;
//        this.segundaGrageaX = gfx;
//        this.segundaGrageaY = gfy;
//    }
//
//    public int getPrimerGrageaX() {
//        return primerGrageaX;
//    }
//
//    public void setPrimerGrageaX(int primerGrageaX) {
//        this.primerGrageaX = primerGrageaX;
//    }
//
//    public int getPrimerGrageaY() {
//        return primerGrageaY;
//    }
//
//    public void setPrimerGrageaY(int primerGrageaY) {
//        this.primerGrageaY = primerGrageaY;
//    }
//
//    public int getSegundaGrageaX() {
//        return segundaGrageaX;
//    }
//
//    public void setSegundaGrageaX(int segundaGrageaX) {
//        this.segundaGrageaX = segundaGrageaX;
//    }
//
//    public int getSegundaGrageaY() {
//        return segundaGrageaY;
//    }
//
//    public void setSegundaGrageaY(int segundaGrageaY) {
//        this.segundaGrageaY = segundaGrageaY;
//    }
//
//    public CopyOnWriteArrayList<Point> getGrageasCombinadas() {
//        return grageasCombinadas;
//    }
//
//    public void setGrageasCombinadas(CopyOnWriteArrayList<Point> grageasCombinadas) {
//        this.grageasCombinadas = grageasCombinadas;
//    }
//
//    public int getPuntaje() {
//        return puntaje;
//    }
//
//    public void setPuntaje(int puntaje) {
//        this.puntaje = puntaje;
//    }
//
//    public int getPuntajeGanar() {
//        return puntajeGanar;
//    }
//
//    public void setPuntajeGanar(int puntajeGanar) {
//        this.puntajeGanar = puntajeGanar;
//    }
//
//    public boolean isFinJuego() {
//        return finJuego.get();
//    }
//
//    public void setFinJuego(boolean finJuego) {
//        this.finJuego.set(finJuego);
//    }
//
//    public CyclicBarrier getBarrierComp() {
//        return barrierComp;
//    }
//
//    public CyclicBarrier getBarrierElim() {
//        return barrierElim;
//    }
//
//    public PatronControlador getControladorJugada() {
//        return controladorJugada;
//    }
//
//    public boolean isHayJugadas() {
//        return hayJugadas;
//    }
//
//    public void setHayJugadas(boolean hayJugadas) {
//        this.hayJugadas = hayJugadas;
//    }
//
//    public int getMovimientos() {
//        return movimientos;
//    }
//
//    public void setMovimientos(int movimientos) {
//        this.movimientos = movimientos;
//    }
//
//    public int getMovimientosTotales() {
//        return movimientosTotales;
//    }
//
//    public void setMovimientosTotales(int movimientosTotales) {
//        this.movimientosTotales = movimientosTotales;
//    }
//
//    public boolean getHuboCombo() {
//        return huboCombo;
//    }
//
//    public void setHuboCombo(boolean b) {
//        huboCombo = b;
//    }
//}
