package org.lab.grageasmagicas.parte_logica;

import org.lab.estructuras.Point;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bermudez Martin, Kurchan Ines, Marinelli Giuliano
 */
public class Comprobador implements Runnable {

    protected Gragea[][] matrizGrageas;
    protected int seccion;
    protected CopyOnWriteArrayList<Point> grageasCombinadas;
    protected CyclicBarrier barrierComp;
    protected AtomicBoolean finJuego;

    public Comprobador(Gragea[][] matrizGrageas, int seccion, CopyOnWriteArrayList grageasCombinadas, CyclicBarrier barrierComp, AtomicBoolean finJuego) {
        this.matrizGrageas = matrizGrageas;
        this.seccion = seccion;
        this.grageasCombinadas = grageasCombinadas;
        this.barrierComp = barrierComp;
        this.finJuego = finJuego;
    }

    @Override
    public void run() {
    }

    synchronized public void dormir() throws InterruptedException {
        wait();
    }

    synchronized public void despertar() {
        notify();
    }

}

