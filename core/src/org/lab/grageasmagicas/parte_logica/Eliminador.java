package org.lab.grageasmagicas.parte_logica;

import org.lab.estructuras.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Eliminador implements Runnable {

    private Gragea[][] matrizGrageas;
    private int columna;
    private CopyOnWriteArrayList<Point> grageasCombinadas;
    private CyclicBarrier barrierElim;
    private int cantGragea;
    private AtomicBoolean finJuego;


    public Eliminador(Gragea[][] matrizGrageas, int columna, CopyOnWriteArrayList<Point> grageasCombinadas, CyclicBarrier barrierElim, int cantGragea, AtomicBoolean finJuego) {
        this.matrizGrageas = matrizGrageas;
        this.columna = columna;
        this.grageasCombinadas = grageasCombinadas;
        this.barrierElim = barrierElim;
        this.cantGragea = cantGragea;
        this.finJuego = finJuego;
    }


    @Override
    public void run() {
        try {
            while (!finJuego.get()) {
                barrierElim.await();
                if (!finJuego.get()) {
                    //me parece mas eficiente hacer esto (claramente mas eficiente :: yuyo)
                    int i = 0;
                    boolean debeEliminar = false;
                    do {
                        //si existe una gragea en una posicion que le corresponda a este eliminador entonces debeEliminar se pone en true
                        debeEliminar = (grageasCombinadas.get(i).y == columna);
                        if (debeEliminar) {
                            eliminarCombinaciones();
                        }
                        i++;
                    } while (!debeEliminar && i < grageasCombinadas.size());
                    barrierElim.await();
                }
            }
        } catch (InterruptedException e) {
            Logger.getLogger(Gragea.class.getName()).log(Level.SEVERE, null, e);
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void eliminarCombinaciones() {
        System.out.print(" <" + columna + "> ");
        List<Integer> combinacionTemp = new ArrayList();
        List<Integer> grageasDuplicadas = new ArrayList();
        for (int i = 0; i < grageasCombinadas.size(); i++) {
            if (grageasCombinadas.get(i).y == columna) {
                if (combinacionTemp.contains(grageasCombinadas.get(i).x)) {
                    grageasDuplicadas.add(grageasCombinadas.get(i).x);
                }
                combinacionTemp.add(grageasCombinadas.get(i).x);
            }
        }
        Random random = new Random();
        int bajar = 0;
        for (int i = matrizGrageas.length - 1; i >= 0; i--) {
            if (combinacionTemp.contains(i)) {
                bajar++;
                if (grageasDuplicadas.contains(i)) {
                    matrizGrageas[i][columna] = new Gragea(100);
                } else {
                    matrizGrageas[i][columna] = new Gragea(random.nextInt(cantGragea));
                }
            } else {
                if (bajar != 0) {
                    Gragea aux = matrizGrageas[i+bajar][columna];
                    matrizGrageas[i+bajar][columna] = matrizGrageas[i][columna];
                    matrizGrageas[i][columna] = aux;
                }
            }
        }
    }

    synchronized public void dormir() throws InterruptedException {
        wait();
    }

    synchronized public void despertar() {
        notify();
    }

}
