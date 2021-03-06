package org.lab.grageasmagicas.parte_visual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.lab.estructuras.Point;
import org.lab.grageasmagicas.parte_logica.Gragea;
import org.lab.grageasmagicas.parte_logica.Juego;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;


public class JuegoVisual implements Screen, Observer {
    //juego visual
    private boolean hayGrageaSeleccionada;
    private int primerGrageaX;
    private int primerGrageaY;
    private int segundaGrageaX;
    private int segundaGrageaY;
    private int anchoCamara;
    private int altoCamara;
    private int animacionesEjecutando;
    private boolean superGrageaActivada;
    private boolean poderMovDiagonalActivado;
    private boolean inputGrageas;
    private boolean inputMenus;
    private boolean tableroListo;
    private boolean drawParEfcBrillante;
    private boolean desbloqueo;
    private GrageaVisual[][] matrizGrageasVisuales;
    private CyclicBarrier barrierRespuestaVisual;
    private float[][] matrizPosGrageaX;
    private float[][] matrizPosGrageaY;
    private long ultimaJugada;
    //juego logico
    private Juego juegoLogico;
    private Gragea[][] matrizGrageasLogica;
    private CopyOnWriteArrayList<Point> grageasCombinadas;
    private int cantColumnas;
    private int cantFilas;
    //administradores
    private AdministradorPantalla adminPantalla;
    private AssetManager assetManager;
    private Viewport vista;
    private Stage escena;
    private SpriteBatch batch;
    private I18NBundle strings;
    //actors
    private Table tblTablero;
    private TextButton btnPuntaje;
    private TextButton btnPuntajeGanar;
    private TextButton btnVolver;
    private TextButton btnSinMovimiento;
    private TextButton btnMovimientos;
    private TextButton btnPoderMovDiagonalUsos;
    private TextButton btnTiempoJuego;
    private ImageTextButton btnFinJuego;
    private ImageButton btnMusica;
    private ImageButton btnPoderMovDiagonal;
    private Image imgFondo;
    //assets
    private Texture txtFondo;
    private Texture txtGragea;
    private Texture txtSuperGragea;
    private Texture txtGrageaBrillo;
    private Texture txtBtnMusicaOn;
    private Texture txtBtnMusicaOff;
    private Texture txtBtnMusicaClick;
    private Texture txtBtnPoderMovDiagonalOn;
    private Texture txtBtnPoderMovDiagonalOff;
    private Texture txtBtnPoderMovDiagonalClick;
    private Texture txtFinJuegoFondo;
    private BitmapFont fntFuenteBase;
    private Music mscMusicaFondo;
    private Music mscMusicaFondoDif1;
    private Music mscMusicaFondoDif2;
    private Music mscMusicaFondoDif3;
    private Music mscMusicaFondoNiveles1;
    private Music mscMusicaFondoNiveles2;
    private Music mscMusicaFondoNiveles3;
    private Music mscMusicaFondoNiveles4;
    private Music mscMusicaFondoNiveles5;
    private Sound sndExplosion;
    private Sound sndSuperExplosion;
    private ParticleEffect parEfcExplosion;
    private ParticleEffect parEfcBrillante;
    //efectos
    private ParticleEffectPool parEfcPoolExplosion;
    private Array<ParticleEffectPool.PooledEffect> actEfcExplosion;
    private ParticleEffectPool.PooledEffect poolEfcExplosion;

    public JuegoVisual(AdministradorPantalla adminPantalla) {
        this.adminPantalla = adminPantalla;
        this.anchoCamara = adminPantalla.getAnchoCamara();
        this.altoCamara = adminPantalla.getAltoCamara();
        this.vista = adminPantalla.getVista();
        this.assetManager = adminPantalla.getAssetManager();
        this.inputGrageas = false;
        this.inputMenus = false;
        this.primerGrageaX = -1;
        this.primerGrageaY = -1;
        this.segundaGrageaX = -1;
        this.segundaGrageaY = -1;
        this.superGrageaActivada = false;
        this.poderMovDiagonalActivado = false;
        this.animacionesEjecutando = 0;
        this.tableroListo = false;
        this.drawParEfcBrillante = false;
        this.desbloqueo = false;


        cargarAssets();

        escena = new Stage(vista);
        Gdx.input.setInputProcessor(escena);
        Gdx.input.setCatchBackKey(true);

        batch = new SpriteBatch();
        parEfcPoolExplosion = new ParticleEffectPool(parEfcExplosion, 25, 100);
        actEfcExplosion = new Array<ParticleEffectPool.PooledEffect>();
    }

    @Override
    public void resize(int width, int height) {
        vista.update(width, height);
    }

    @Override
    public void show() {
        //implementado en update
    }

    public void gameLoop() {
        try {

            //crea la matriz visual y la estrutura de tabla si no fue creada aun
            if (!tableroListo) {
                inicializar();
            }

            //actualiza el btnPuntaje segun el obtenido del juego logico
            btnPuntaje.setText(juegoLogico.getPuntaje() + "");
            btnPuntaje.setWidth(btnPuntaje.getPrefWidth());
            btnPuntaje.setHeight(btnPuntaje.getPrefHeight());
            btnPuntaje.setPosition(50, altoCamara - btnPuntaje.getHeight() - 25);

            //actualiza el btnMovimientos segun el obtenido del juego logico
            if (juegoLogico.getModoJuego() == 0) {
                btnMovimientos.setText(juegoLogico.getMovimientos() + " / " + juegoLogico.getMovimientosTotales());
                btnMovimientos.setWidth(btnMovimientos.getPrefWidth());
                btnMovimientos.setHeight(btnMovimientos.getPrefHeight());
                btnMovimientos.setPosition(50, altoCamara - btnMovimientos.getHeight() - btnPuntaje.getHeight() - 25);
            }

            //actualiza el btnPoderMovDiagonal segun la cantidad que queden disponibles
            if (juegoLogico.getPoderMovDiagonalUsos() == juegoLogico.getPoderMovDiagonalUsosTotales()) {
                btnPoderMovDiagonal.setTouchable(Touchable.disabled);
                btnPoderMovDiagonal.getImage().setColor(Color.GRAY);
                btnPoderMovDiagonal.setChecked(false);
                poderMovDiagonalActivado = false;
                btnPoderMovDiagonalUsos.getLabel().setFontScale(2, 2);
                btnPoderMovDiagonalUsos.getStyle().fontColor = Color.BLACK;
            } else {
                btnPoderMovDiagonal.setTouchable(Touchable.enabled);
                btnPoderMovDiagonal.getImage().setColor(Color.WHITE);
                btnPoderMovDiagonalUsos.getLabel().setFontScale(2.5f, 2.5f);
                btnPoderMovDiagonalUsos.getStyle().fontColor = Color.WHITE;
            }
            btnPoderMovDiagonalUsos.setText(juegoLogico.getPoderMovDiagonalUsosTotales() - juegoLogico.getPoderMovDiagonalUsos() + "");
            btnPoderMovDiagonalUsos.setWidth(btnPoderMovDiagonalUsos.getPrefWidth());
            btnPoderMovDiagonalUsos.setHeight(btnPoderMovDiagonalUsos.getPrefHeight());
            btnPoderMovDiagonalUsos.setPosition(btnPoderMovDiagonal.getX() + btnPoderMovDiagonal.getWidth() - btnPoderMovDiagonalUsos.getWidth()
                    , btnPoderMovDiagonal.getY());

            if (juegoLogico.getModoJuego() == 0 && juegoLogico.getPuntaje() >= juegoLogico.getPuntajeGanar()) {
                parEfcBrillante.setPosition(
                        btnPuntajeGanar.getX() + btnPuntajeGanar.getWidth() / 2,
                        btnPuntajeGanar.getY() + btnPuntajeGanar.getHeight() / 2);
                parEfcBrillante.start();
                drawParEfcBrillante = true;
            }

            //intercambia las grageas cuando se realizo un movimiento
            intercambiarGrageas();

            //verifica que grageas fueron eliminadas y las reemplaza por las nuevas grageas
            //aleatorias que se generaron
            crearNuevasGrageas();

            //verifica si ocurrieron combinaciones e intercambia aquellas grageas que se van a
            //eliminar con sus superiores y luego las oculta para que puedan ser reemplazadas por
            //las nuevas grageas aleatorias
            eliminarGrageas();

            //este metodo se ejecuta luego de realizar todas las animaciones para asegurar que no
            //se rompa la consistencia grafica del juego
            corregirPosiciones();

            //verificar si quedan movimientos posibles
            verificarMovimientoPosible();

            if (juegoLogico.isFinJuego()) {
                mostrarResultado();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Crea la matriz visual, la estrutura de tabla, y el resto de los elementos visuales
     */
    public void inicializar() {
        matrizGrageasVisuales = new GrageaVisual[cantFilas][cantColumnas];

        imgFondo = new Image(txtFondo);
        imgFondo.setScale(anchoCamara / imgFondo.getWidth(), altoCamara / imgFondo.getHeight());
        escena.addActor(imgFondo);

        if (juegoLogico.getModoJuego() == 0) {
            int nivelAux = juegoLogico.getNivel();
            if (nivelAux < 10) {
                mscMusicaFondo = mscMusicaFondoNiveles1;
            } else if (nivelAux < 20) {
                mscMusicaFondo = mscMusicaFondoNiveles2;
            } else if (nivelAux < 30) {
                mscMusicaFondo = mscMusicaFondoNiveles3;
            } else if (nivelAux < 40) {
                mscMusicaFondo = mscMusicaFondoNiveles4;
            } else {
                mscMusicaFondo = mscMusicaFondoNiveles5;
            }
        } else if (juegoLogico.getModoJuego() == 1) {
            switch (juegoLogico.getDificultad()) {
                case 0:
                    mscMusicaFondo = mscMusicaFondoDif1;
                    break;
                case 1:
                    mscMusicaFondo = mscMusicaFondoDif2;
                    break;
                case 2:
                    mscMusicaFondo = mscMusicaFondoDif3;
                    break;
            }
        } else {
            mscMusicaFondo = mscMusicaFondoDif1;
        }
        mscMusicaFondo.setLooping(true);
        mscMusicaFondo.setVolume(1f);
        if (adminPantalla.getInterfazDb().consultarOpcionSonido(adminPantalla.getIdUsuario())) {
            mscMusicaFondo.play();
        }

        tblTablero = new Table();
        escena.addActor(tblTablero);

        TextButton.TextButtonStyle btnStlVolver = new TextButton.TextButtonStyle();
        btnStlVolver.font = fntFuenteBase;
        btnVolver = new TextButton(strings.get("btn_volver"), btnStlVolver);
        btnVolver.getLabel().setFontScale(2, 2);
        btnVolver.setWidth(btnVolver.getPrefWidth());
        btnVolver.setHeight(btnVolver.getPrefHeight());
        btnVolver.setPosition(anchoCamara - btnVolver.getWidth() - 50, altoCamara - btnVolver.getHeight() - 25);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (isInputMenus()) {
                        if (animacionesEjecutando == 0) {
                            juegoLogico.terminar();

                            barrierRespuestaVisual.await();

                            switch (juegoLogico.getModoJuego()) {
                                case 0:
                                    adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuNiveles()));
                                    break;
                                case 1:
                                    adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuNivelesTiempo()));
                                    break;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        escena.addActor(btnVolver);

        TextureRegionDrawable trBtnMusicaOn = new TextureRegionDrawable(new TextureRegion(txtBtnMusicaOn));
        TextureRegionDrawable trBtnMusicaOff = new TextureRegionDrawable(new TextureRegion(txtBtnMusicaOff));
        TextureRegionDrawable trBtnMusicaClick = new TextureRegionDrawable(new TextureRegion(txtBtnMusicaClick));
        btnMusica = new ImageButton(trBtnMusicaOn, trBtnMusicaClick, trBtnMusicaOff);
        if (adminPantalla.getInterfazDb().consultarOpcionSonido(adminPantalla.getIdUsuario())) {
            btnMusica.setChecked(false);
        } else {
            btnMusica.setChecked(true);
        }
        btnMusica.setDisabled(true);
        btnMusica.setPosition(anchoCamara - btnMusica.getWidth() - 75, altoCamara - btnMusica.getHeight() - btnVolver.getHeight() - 50);
        btnMusica.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isInputMenus()) {
                    btnMusica.setDisabled(false);
                    if (adminPantalla.getInterfazDb().consultarOpcionSonido(adminPantalla.getIdUsuario())) {
                        mscMusicaFondo.pause();
                        adminPantalla.getInterfazDb().setOpcionSonido(adminPantalla.getIdUsuario(), false);
                        btnMusica.setChecked(true);
                    } else {
                        mscMusicaFondo.play();
                        adminPantalla.getInterfazDb().setOpcionSonido(adminPantalla.getIdUsuario(), true);
                        btnMusica.setChecked(false);
                    }
                    btnMusica.setDisabled(true);
                }
            }
        });
        escena.addActor(btnMusica);

        TextureRegionDrawable trBtnPoderMovDiagonalOn = new TextureRegionDrawable(new TextureRegion(txtBtnPoderMovDiagonalOn));
        TextureRegionDrawable trBtnPoderMovDiagonalOff = new TextureRegionDrawable(new TextureRegion(txtBtnPoderMovDiagonalOff));
        TextureRegionDrawable trBtnPoderMovDiagonalClick = new TextureRegionDrawable(new TextureRegion(txtBtnPoderMovDiagonalClick));
        btnPoderMovDiagonal = new ImageButton(trBtnPoderMovDiagonalOff, trBtnPoderMovDiagonalClick, trBtnPoderMovDiagonalOn);
        btnPoderMovDiagonal.setDisabled(true);
        btnPoderMovDiagonal.setPosition(50, 75);
        btnPoderMovDiagonal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isInputMenus()) {
                    btnPoderMovDiagonal.setDisabled(false);
                    if (poderMovDiagonalActivado) {
                        btnPoderMovDiagonal.setChecked(false);
                        poderMovDiagonalActivado = false;
                        desbrillarGrageas();
                        if (primerGrageaX != -1)
                            brillarGrageasAdyacentes(primerGrageaX, primerGrageaY);
                    } else {
                        if (juegoLogico.getPoderMovDiagonalUsos() < juegoLogico.getPoderMovDiagonalUsosTotales()) {
                            btnPoderMovDiagonal.setChecked(true);
                            poderMovDiagonalActivado = true;
                            desbrillarGrageas();
                            if (primerGrageaX != -1)
                                brillarGrageasDiagonales(primerGrageaX, primerGrageaY);
                        }
                    }
                    btnPoderMovDiagonal.setDisabled(true);
                }
            }
        });
        escena.addActor(btnPoderMovDiagonal);

        TextButton.TextButtonStyle btnStlSinMoviminto = new TextButton.TextButtonStyle();
        btnStlSinMoviminto.font = fntFuenteBase;
        btnStlSinMoviminto.fontColor = Color.RED;
        btnSinMovimiento = new TextButton(strings.get("btn_sin_movimientos"), btnStlSinMoviminto);
        btnSinMovimiento.setPosition(anchoCamara / 2 - btnSinMovimiento.getWidth() / 2, altoCamara - btnSinMovimiento.getHeight() - 25);
        btnSinMovimiento.setVisible(false);
        btnSinMovimiento.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    btnSinMovimiento.setVisible(false);
                    desbrillarGrageas();
                    barrierRespuestaVisual.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        escena.addActor(btnSinMovimiento);

        TextureRegionDrawable trBtnFinJuegoFondo = new TextureRegionDrawable(new TextureRegion(txtFinJuegoFondo));
        ImageTextButton.ImageTextButtonStyle btnStlFinJuego = new ImageTextButton.ImageTextButtonStyle(
                trBtnFinJuegoFondo, trBtnFinJuegoFondo, trBtnFinJuegoFondo, fntFuenteBase);
        btnStlFinJuego.font = fntFuenteBase;
        btnStlFinJuego.fontColor = Color.GOLD;
        btnFinJuego = new ImageTextButton(strings.get("btn_fin_juego"), btnStlFinJuego);
        btnFinJuego.getLabel().setFontScale(3, 3);
        btnFinJuego.setWidth(btnFinJuego.getPrefWidth());
        btnFinJuego.setHeight(btnFinJuego.getPrefHeight());
        btnFinJuego.setPosition(anchoCamara / 2 - btnFinJuego.getWidth() / 2, altoCamara / 2 - btnFinJuego.getHeight() / 2);
        btnFinJuego.setVisible(false);
        btnFinJuego.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (animacionesEjecutando == 0) {
                        juegoLogico.terminar();

                        barrierRespuestaVisual.await();

                        switch (juegoLogico.getModoJuego()) {
                            case 0:
                                adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuNiveles()));
                                break;
                            case 1:
                                adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuNivelesTiempo()));
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        escena.addActor(btnFinJuego);

        TextButton.TextButtonStyle btnStlPuntaje = new TextButton.TextButtonStyle();
        btnStlPuntaje.font = fntFuenteBase;
        btnStlPuntaje.fontColor = Color.GOLD;
        btnPuntaje = new TextButton(juegoLogico.getPuntaje() + "", btnStlPuntaje);
        btnPuntaje.getLabel().setFontScale(2, 2);
        btnPuntaje.setWidth(btnPuntaje.getPrefWidth());
        btnPuntaje.setHeight(btnPuntaje.getPrefHeight());
        btnPuntaje.setPosition(50, altoCamara - btnPuntaje.getHeight() - 25);
        escena.addActor(btnPuntaje);

        if (juegoLogico.getModoJuego() == 0) {
            TextButton.TextButtonStyle btnStlPuntajeGanar = new TextButton.TextButtonStyle();
            btnStlPuntajeGanar.font = fntFuenteBase;
            btnStlPuntajeGanar.fontColor = Color.GOLD;
            btnPuntajeGanar = new TextButton(juegoLogico.getPuntajeGanar() + "", btnStlPuntajeGanar);
            btnPuntajeGanar.getLabel().setFontScale(2, 2);
            btnPuntajeGanar.setWidth(btnPuntajeGanar.getPrefWidth());
            btnPuntajeGanar.setHeight(btnPuntajeGanar.getPrefHeight());
            btnPuntajeGanar.setPosition(anchoCamara / 2 - btnPuntajeGanar.getWidth() / 2, 25);
            escena.addActor(btnPuntajeGanar);
        }

        if (juegoLogico.getModoJuego() == 0) {
            TextButton.TextButtonStyle btnStlMovimientos = new TextButton.TextButtonStyle();
            btnStlMovimientos.font = fntFuenteBase;
            btnStlMovimientos.fontColor = Color.BLUE;
            btnMovimientos = new TextButton(juegoLogico.getMovimientos() + " / " + juegoLogico.getMovimientosTotales(), btnStlMovimientos);
            btnMovimientos.getLabel().setFontScale(2, 2);
            btnMovimientos.setWidth(btnMovimientos.getPrefWidth());
            btnMovimientos.setHeight(btnMovimientos.getPrefHeight());
            btnMovimientos.setPosition(50, altoCamara - btnMovimientos.getHeight() - btnPuntaje.getHeight() - 25);
            escena.addActor(btnMovimientos);
        }

        if (juegoLogico.getModoJuego() == 1) {
            TextButton.TextButtonStyle btnStlTiempoJuego = new TextButton.TextButtonStyle();
            btnStlTiempoJuego.font = fntFuenteBase;
            btnStlTiempoJuego.fontColor = Color.BLACK;
            btnTiempoJuego = new TextButton(juegoLogico.getTiempoJuego() / 60 + ":" + juegoLogico.getTiempoJuego() % 60, btnStlTiempoJuego);
            btnTiempoJuego.getLabel().setFontScale(2, 2);
            btnTiempoJuego.setWidth(btnTiempoJuego.getPrefWidth());
            btnTiempoJuego.setHeight(btnTiempoJuego.getPrefHeight());
            btnTiempoJuego.setPosition(50, altoCamara - btnTiempoJuego.getHeight() - btnPuntaje.getHeight() - 25);
            escena.addActor(btnTiempoJuego);
        }

        TextButton.TextButtonStyle btnStlPoderMovDiagonalUsos = new TextButton.TextButtonStyle();
        btnStlPoderMovDiagonalUsos.font = fntFuenteBase;
        btnStlPoderMovDiagonalUsos.fontColor = Color.BLACK;
        btnPoderMovDiagonalUsos = new TextButton(juegoLogico.getPoderMovDiagonalUsosTotales() - juegoLogico.getPoderMovDiagonalUsos() + "", btnStlPoderMovDiagonalUsos);
        btnPoderMovDiagonalUsos.getLabel().setFontScale(2, 2);
        btnPoderMovDiagonalUsos.setWidth(btnPoderMovDiagonalUsos.getPrefWidth());
        btnPoderMovDiagonalUsos.setHeight(btnPoderMovDiagonalUsos.getPrefHeight());
        btnPoderMovDiagonalUsos.setPosition(btnPoderMovDiagonal.getX() + btnPoderMovDiagonal.getWidth() - btnPoderMovDiagonalUsos.getWidth()
                , btnPoderMovDiagonal.getY());
        escena.addActor(btnPoderMovDiagonalUsos);

        tblTablero.row();
        for (int i = 0; i < cantFilas; i++) {
            for (int j = 0; j < cantColumnas; j++) {
                matrizGrageasVisuales[i][j] = new GrageaVisual(matrizGrageasLogica[i][j].getTipo(), txtGragea, txtSuperGragea, txtGrageaBrillo);
                matrizGrageasVisuales[i][j].addListener(new GrageaVisualListener(matrizGrageasVisuales[i][j], this, i, j));
                matrizGrageasVisuales[i][j].setTamano(640 / cantFilas, 480 / cantColumnas);
                tblTablero.add(matrizGrageasVisuales[i][j]);
            }
            tblTablero.row();
        }
        tblTablero.padBottom(5f);
        tblTablero.setFillParent(true);
        tblTablero.pack();

        matrizPosGrageaX = new float[cantFilas][cantColumnas];
        matrizPosGrageaY = new float[cantFilas][cantColumnas];
        for (int i = 0; i < cantFilas; i++) {
            for (int j = 0; j < cantColumnas; j++) {
                matrizPosGrageaX[i][j] = matrizGrageasVisuales[i][j].getX();
                matrizPosGrageaY[i][j] = matrizGrageasVisuales[i][j].getY();
            }
        }

        tableroListo = true;
    }

    /**
     * Intercambia grageas graficamente cuando se realizo un movimiento por parte del usuario
     *
     * @throws InterruptedException
     */
    public void intercambiarGrageas() throws InterruptedException {
        //solo actua cuando las posiciones ingresadas de las grageas son validas
        if (juegoLogico.getPrimerGrageaX() != -1 && juegoLogico.getSegundaGrageaX() != -1) {
            GrageaVisual priGragea = matrizGrageasVisuales[juegoLogico.getPrimerGrageaX()][juegoLogico.getPrimerGrageaY()];
            GrageaVisual segGragea = matrizGrageasVisuales[juegoLogico.getSegundaGrageaX()][juegoLogico.getSegundaGrageaY()];
            GrageaVisualListener priGrageaListener = (GrageaVisualListener) (priGragea.getListeners().get(0));
            GrageaVisualListener segGrageaListener = (GrageaVisualListener) (segGragea.getListeners().get(0));
            priGragea.brillar(true);
            segGragea.brillar(true);
                /*priGragea.addAction(Actions.moveTo
                        (segGragea.getX(), segGragea.getY(), 0.5f, Interpolation.bounceOut));
                segGragea.addAction(Actions.moveTo
                        (priGragea.getX(), priGragea.getY(), 0.5f, Interpolation.bounceOut));*/
            priGragea.addAction(new AnimacionMover
                    (segGragea.getX(), segGragea.getY(), 0.5f, Interpolation.bounceOut, this));
            animacionesEjecutando++;
            segGragea.addAction(new AnimacionMover
                    (priGragea.getX(), priGragea.getY(), 0.5f, Interpolation.bounceOut, this));
            animacionesEjecutando++;
            priGrageaListener.setFilaColumnaGragea(juegoLogico.getSegundaGrageaX(), juegoLogico.getSegundaGrageaY());
            segGrageaListener.setFilaColumnaGragea(juegoLogico.getPrimerGrageaX(), juegoLogico.getPrimerGrageaY());
            matrizGrageasVisuales[juegoLogico.getPrimerGrageaX()][juegoLogico.getPrimerGrageaY()] = segGragea;
            matrizGrageasVisuales[juegoLogico.getSegundaGrageaX()][juegoLogico.getSegundaGrageaY()] = priGragea;
            if (animacionesEjecutando > 0) {
                dormir();
                //sleep(500);
            }
            desbrillarGrageas();
        }
    }

    /**
     * Verifica que grageas fueron eliminadas a traves de la propiedad "visible" y las reemplaza por
     * las nuevas grageas aleatorias que se generaron
     *
     * @throws InterruptedException
     */
    public void crearNuevasGrageas() throws InterruptedException {
        boolean hayNuevas = false;
        float posXNuevaGrageaVisual;
        float posYNuevaGrageaVisual;
        for (int i = 0; i < cantFilas; i++) {
            for (int j = 0; j < cantColumnas; j++) {
                if (matrizGrageasVisuales[i][j] != null) {
                    if (!matrizGrageasVisuales[i][j].isVisible()) {
                        hayNuevas = true;
                        posXNuevaGrageaVisual = matrizGrageasVisuales[i][j].getX();
                        posYNuevaGrageaVisual = matrizGrageasVisuales[i][j].getY();
                        matrizGrageasVisuales[i][j].setTipo(matrizGrageasLogica[i][j].getTipo());
                        GrageaVisualListener grageaListener = (GrageaVisualListener) (matrizGrageasVisuales[i][j].getListeners().get(0));
                        grageaListener.setFilaColumnaGragea(i, j);
                        matrizGrageasVisuales[i][j].setPosition(posXNuevaGrageaVisual, altoCamara);
                        matrizGrageasVisuales[i][j].setVisible(true);
                            /*matrizGrageasVisuales[i][j].addAction(Actions.moveTo
                                    (posXNuevaGrageaVisual, posYNuevaGrageaVisual, 0.5f, Interpolation.bounceOut));*/
                        matrizGrageasVisuales[i][j].addAction(new AnimacionMover
                                (posXNuevaGrageaVisual, posYNuevaGrageaVisual, 0.5f, Interpolation.bounceOut, this));
                        animacionesEjecutando++;
                        //Sound sGrageasNuevas = Gdx.audio.newSound(Gdx.files.internal("grageasNuevas.mp3"));
                    }
                }
            }
        }

        if (hayNuevas) {
            if (animacionesEjecutando > 0) {
                dormir();
                //sleep(500);
            }
        }
    }

    /**
     * Verifica si ocurrieron combinaciones e intercambia aquellas grageas que se van a
     * eliminar con sus superiores y luego las oculta para que puedan ser reemplazadas por
     * las nuevas grageas aleatorias
     *
     * @throws InterruptedException
     */
    public void eliminarGrageas() throws InterruptedException {
        //solo actua cuando se encuentran elementos en el arreglo de grageasCombinadas
        if (!juegoLogico.getGrageasCombinadas().isEmpty()) {
            GrageaVisualListener priGrageaListener;
            GrageaVisualListener segGrageaListener;
            float posXAnt;
            float posYAnt;
            for (int j = 0; j < cantColumnas; j++) {
                List<Integer> combinacionTemp = new ArrayList();
                for (int i = 0; i < grageasCombinadas.size(); i++) {
                    if (grageasCombinadas.get(i).y == j) {
                        combinacionTemp.add(grageasCombinadas.get(i).x);
                    }
                }
                /*Collections.sort(combinacionTemp);
                HashSet hs = new HashSet();
                hs.addAll(combinacionTemp);
                combinacionTemp.clear();
                combinacionTemp.addAll(hs);*/
                int bajar = 0;
                for (int i = cantFilas - 1; i >= 0; i--) {
                    if (combinacionTemp.contains(i)) {
                        matrizGrageasVisuales[i][j].setVisible(false);
                        bajar++;
                        poolEfcExplosion = parEfcPoolExplosion.obtain();
                        poolEfcExplosion.setPosition(
                                matrizGrageasVisuales[i][j].getX() + matrizGrageasVisuales[i][j].getWidth() / 2,
                                matrizGrageasVisuales[i][j].getY() + matrizGrageasVisuales[i][j].getHeight() / 2);
                        actEfcExplosion.add(poolEfcExplosion);
                    } else {
                        if (bajar != 0) {
                            posXAnt = matrizGrageasVisuales[i][j].getX();
                            posYAnt = matrizGrageasVisuales[i][j].getY();
                                /*matrizGrageasVisuales[i][j].addAction(Actions.moveTo
                                        (matrizGrageasVisuales[i + bajar][j].getX(), matrizGrageasVisuales[i + bajar][j].getY(),
                                                0.5f, Interpolation.bounceOut));*/
                            matrizGrageasVisuales[i][j].addAction(new AnimacionMover(
                                    matrizGrageasVisuales[i + bajar][j].getX(), matrizGrageasVisuales[i + bajar][j].getY(),
                                    0.5f, Interpolation.bounceOut, this));
                            animacionesEjecutando++;
                            matrizGrageasVisuales[i + bajar][j].setPosition(posXAnt, posYAnt);
                            priGrageaListener = (GrageaVisualListener) (matrizGrageasVisuales[i][j].getListeners().get(0));
                            segGrageaListener = (GrageaVisualListener) (matrizGrageasVisuales[i + bajar][j].getListeners().get(0));
                            priGrageaListener.setFilaColumnaGragea(i + bajar, j);
                            segGrageaListener.setFilaColumnaGragea(i, j);
                            GrageaVisual aux = matrizGrageasVisuales[i][j];
                            matrizGrageasVisuales[i][j] = matrizGrageasVisuales[i + bajar][j];
                            matrizGrageasVisuales[i + bajar][j] = aux;
                        }
                    }
                }
                if (juegoLogico.getHuboCombo() || juegoLogico.isSuperGrageaActivada()) {
                    if (adminPantalla.getInterfazDb().consultarOpcionVibracion(adminPantalla.getIdUsuario())) {
                        Gdx.input.vibrate(200);
                    }
                    if (adminPantalla.getInterfazDb().consultarOpcionSonido(adminPantalla.getIdUsuario())) {
                        sndSuperExplosion.play();
                    }
                    juegoLogico.setHuboCombo(false);
                }
            }
            if (adminPantalla.getInterfazDb().consultarOpcionSonido(adminPantalla.getIdUsuario())) {
                sndExplosion.play();
            }
            if (animacionesEjecutando > 0) {
                dormir();
                //sleep(500);
            }
        }
    }


    /**
     * Corrige las posiciones de las grageas visuales segun la matriz de posiciones inicial
     */
    public void corregirPosiciones() {
        for (int i = 0; i < cantFilas; i++) {
            for (int j = 0; j < cantColumnas; j++) {
                matrizGrageasVisuales[i][j].setPosition(matrizPosGrageaX[i][j], matrizPosGrageaY[i][j]);
            }
        }
    }

    /**
     * Verifica si quedan movimientos posibles y de no ser asi muestra el boton para para mezclar
     * nuevamente las grageas
     */
    public void verificarMovimientoPosible() {
        if (!juegoLogico.isHayJugadas()) {
            inputGrageas = false;
            btnSinMovimiento.setVisible(true);
        } else {
            btnSinMovimiento.setVisible(false);
        }
    }

    /**
     *
     */
    public void mostrarResultado() throws InterruptedException {
        btnPuntaje.getLabel().setFontScale(4, 4);
        btnPuntaje.setWidth(btnPuntaje.getPrefWidth());
        btnPuntaje.setHeight(btnPuntaje.getPrefHeight());
        btnPuntaje.addAction(new AnimacionMover(anchoCamara / 2 - btnPuntaje.getWidth() / 2,
                altoCamara - btnPuntaje.getHeight() - 50, 0.5f, Interpolation.bounceOut, this));
        animacionesEjecutando++;
        switch (juegoLogico.getModoJuego()) {
            case 0:
                if (juegoLogico.getPuntaje() < juegoLogico.getPuntajeGanar()) {
                    btnFinJuego.setText(strings.get("btn_fin_derrota"));
                    btnFinJuego.getLabel().setColor(Color.RED);
                } else {
                    btnFinJuego.setText(strings.get("btn_fin_victoria"));
                    btnFinJuego.getLabel().setColor(Color.GREEN);
                    if (adminPantalla.isSesion()) {
                        adminPantalla.getInterfazDb().desbloquearNivel(adminPantalla.getIdUsuario(), juegoLogico.getNivel());
                    }
                }
                btnMovimientos.addAction(new AnimacionMover(50, altoCamara - btnMovimientos.getHeight() - 25, 0.5f, Interpolation.bounceOut, this));
                animacionesEjecutando++;
                break;
            case 1:
                btnFinJuego.setText(strings.get("btn_fin_juego"));
                btnFinJuego.getLabel().setColor(Color.GRAY);
                if (adminPantalla.isSesion()) {
                    adminPantalla.getInterfazDb().insertarPuntaje(adminPantalla.getIdUsuario(), juegoLogico.getPuntaje(), juegoLogico.getDificultad());
                }
                btnTiempoJuego.addAction(new AnimacionMover(50, altoCamara - btnTiempoJuego.getHeight() - 25, 0.5f, Interpolation.bounceOut, this));
                animacionesEjecutando++;
                break;
        }
        btnFinJuego.setVisible(true);
        if (animacionesEjecutando > 0) {
            dormir();
        }
    }

    /**
     * Permite sincronizar las animaciones con el juegoVisual, haciendo que la ultima animacion en
     * terminar lo despierte
     */
    synchronized public void animacionTermina() {
        animacionesEjecutando--;
        if (animacionesEjecutando == 0) {
            notify();
        }
    }

    /**
     * Duerme al juegoVisual, dejandolo a la espera de un notify
     *
     * @throws InterruptedException
     */
    synchronized public void dormir() throws InterruptedException {
        wait();
    }

    public void brillarGrageasAdyacentes(int filaGragea, int columnaGragea) {
        if (filaGragea < cantFilas - 1)
            matrizGrageasVisuales[filaGragea + 1][columnaGragea].brillar(true);
        if (filaGragea > 0)
            matrizGrageasVisuales[filaGragea - 1][columnaGragea].brillar(true);
        if (columnaGragea < cantColumnas - 1)
            matrizGrageasVisuales[filaGragea][columnaGragea + 1].brillar(true);
        if (columnaGragea > 0)
            matrizGrageasVisuales[filaGragea][columnaGragea - 1].brillar(true);
    }

    public void brillarGrageasDiagonales(int filaGragea, int columnaGragea) {
        if (filaGragea < cantFilas - 1 &&
                columnaGragea < cantColumnas - 1)
            matrizGrageasVisuales[filaGragea + 1][columnaGragea + 1].brillar(true);
        if (filaGragea > 0 && columnaGragea > 0)
            matrizGrageasVisuales[filaGragea - 1][columnaGragea - 1].brillar(true);
        if (columnaGragea < cantColumnas - 1 && filaGragea > 0)
            matrizGrageasVisuales[filaGragea - 1][columnaGragea + 1].brillar(true);
        if (columnaGragea > 0 && filaGragea < cantFilas - 1)
            matrizGrageasVisuales[filaGragea + 1][columnaGragea - 1].brillar(true);
    }

    public void desbrillarGrageas() {
        for (int i = 0; i < cantFilas; i++) {
            for (int j = 0; j < cantColumnas; j++) {
                matrizGrageasVisuales[i][j].brillar(false);
            }
        }
    }

    /**
     * Re-dibuja el juego a 30fps (default), se altera los elementos graficos de la "escena" y luego
     * son dibujados en el render
     *
     * @param delta
     */
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (tableroListo) {
            if (juegoLogico.getModoJuego() == 1) {
                if (!juegoLogico.isFinJuego()) {
                    String segundos = juegoLogico.getTiempoJuego() % 60 + "";
                    if (Integer.parseInt(segundos) < 10) {
                        segundos = "0" + segundos;
                    }
                    if (juegoLogico.getTiempoJuego() <= 10) {
                        btnTiempoJuego.getStyle().fontColor = Color.RED;
                    } else {
                        btnTiempoJuego.getStyle().fontColor = Color.BLACK;
                    }
                    btnTiempoJuego.setText(juegoLogico.getTiempoJuego() / 60 + ":" + segundos);
                    btnTiempoJuego.setWidth(btnTiempoJuego.getPrefWidth());
                    btnTiempoJuego.setHeight(btnTiempoJuego.getPrefHeight());
                    btnTiempoJuego.setPosition(50, altoCamara - btnTiempoJuego.getHeight() - btnPuntaje.getHeight() - 25);
                }

                if (!juegoLogico.isFinJuego() && juegoLogico.getTiempoJuego() <= 0 && barrierRespuestaVisual.getNumberWaiting() == 1) {
                    try {
                        if (!desbloqueo) {
                            desbloqueo = true;
                            barrierRespuestaVisual.await();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }

            escena.act(delta);
            escena.setViewport(vista);
            escena.draw();

            //dibuja los efectos de particulas
            batch.begin();
            float deltaTime = Gdx.graphics.getDeltaTime();
            batch.setProjectionMatrix(adminPantalla.getCamara().combined);
            for (int i = 0; i < actEfcExplosion.size; ) {
                ParticleEffectPool.PooledEffect effect = actEfcExplosion.get(i);
                if (effect.isComplete()) {
                    parEfcPoolExplosion.free(effect);
                    actEfcExplosion.removeIndex(i);
                } else {
                    effect.draw(batch, deltaTime);
                    ++i;
                }
            }

            if (drawParEfcBrillante) {
                parEfcBrillante.draw(batch, deltaTime);
                if (parEfcBrillante.isComplete()) {
                    parEfcBrillante.reset();
                }
            }

            if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
                if (isInputMenus()) {
                    try {
                        juegoLogico.terminar();

                        barrierRespuestaVisual.await();

                        switch (juegoLogico.getModoJuego()) {
                            case 0:
                                adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuNiveles()));
                                break;
                            case 1:
                                adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuNivelesTiempo()));
                                break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }

            batch.end();



            /*mostrar jugada posible luego de 5000 milisegundos (5 segundos)*/
            if (System.currentTimeMillis() - ultimaJugada > 5000) {
                //obtener la jugada
                Point auxIni;
                Point auxFin;
                //si existe recomienda una jugada en linea recta
                if (juegoLogico.hayJugadaRecta()) {
                    auxIni = juegoLogico.getJugadaRecta().getMovimientoIni();
                    auxFin = juegoLogico.getJugadaRecta().getMovimientoFin();
                    matrizGrageasVisuales[auxIni.x][auxIni.y].brillar(true);
                    matrizGrageasVisuales[auxFin.x][auxFin.y].brillar(true);
                }
            }
        }

    }

    /**
     * Obtiene los cambios del juegoLogico y actualiza las estructuras locales para luego reflejar
     * los cambios visualmente a traves del gameLoop
     *
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {
        synchronized (this) {
            if (assetManager.update()) {
                //seteamos los nuevos datos obtenidos desde el juego logico observado
                juegoLogico = (Juego) observable;
                matrizGrageasLogica = juegoLogico.getMatrizGrageas();
                grageasCombinadas = juegoLogico.getGrageasCombinadas();
                cantColumnas = matrizGrageasLogica[0].length;
                cantFilas = matrizGrageasLogica.length;
                ultimaJugada = System.currentTimeMillis();
                //realizamos los cambios en el juego a partir de lo obtenido
                gameLoop();
            }
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        txtFondo.dispose();
        txtGragea.dispose();
        txtSuperGragea.dispose();
        txtGrageaBrillo.dispose();
        txtBtnMusicaOn.dispose();
        txtBtnMusicaOff.dispose();
        txtBtnMusicaClick.dispose();
        txtBtnPoderMovDiagonalOn.dispose();
        txtBtnPoderMovDiagonalOff.dispose();
        txtBtnPoderMovDiagonalClick.dispose();
        txtFinJuegoFondo.dispose();
        fntFuenteBase.dispose();
        mscMusicaFondoDif1.dispose();
        mscMusicaFondoDif2.dispose();
        mscMusicaFondoDif3.dispose();
        mscMusicaFondoNiveles1.dispose();
        mscMusicaFondoNiveles2.dispose();
        mscMusicaFondoNiveles3.dispose();
        mscMusicaFondoNiveles4.dispose();
        mscMusicaFondoNiveles5.dispose();
        sndExplosion.dispose();
        sndSuperExplosion.dispose();
        parEfcExplosion.dispose();
        parEfcBrillante.dispose();
        escena.dispose();
        //assetManager.clear();
        assetManager.unload("imagenes/fondo_juego.png");
        assetManager.unload("imagenes/gragea.png");
        assetManager.unload("imagenes/super_gragea.png");
        assetManager.unload("imagenes/gragea_brillo.png");
        assetManager.unload("imagenes/musica_on.png");
        assetManager.unload("imagenes/musica_off.png");
        assetManager.unload("imagenes/musica_click.png");
        assetManager.unload("imagenes/btn_poder_mov_diagonal_on.png");
        assetManager.unload("imagenes/btn_poder_mov_diagonal_off.png");
        assetManager.unload("imagenes/btn_poder_mov_diagonal_click.png");
        assetManager.unload("imagenes/fin_btn_fondo.png");
        assetManager.unload("fuentes/texto_bits.fnt");
        assetManager.unload("sonidos/musica_fondo_dif1.mp3");
        assetManager.unload("sonidos/musica_fondo_dif2.mp3");
        assetManager.unload("sonidos/musica_fondo_dif3.mp3");
        assetManager.unload("sonidos/musica_fondo_lvl_1_10.mp3");
        assetManager.unload("sonidos/musica_fondo_lvl_10_20.mp3");
        assetManager.unload("sonidos/musica_fondo_lvl_20_30.mp3");
        assetManager.unload("sonidos/musica_fondo_lvl_30_40.mp3");
        assetManager.unload("sonidos/musica_fondo_lvl_40_50.mp3");
        assetManager.unload("sonidos/explosion.mp3");
        assetManager.unload("sonidos/super_explosion.mp3");
        assetManager.unload("efectos/explosion.effect");
        assetManager.unload("efectos/brillante.effect");
        assetManager.unload("strings/strings");
    }

    public void cargarAssets() {
        //loader para efectos de particulas
        ParticleEffectLoader.ParticleEffectParameter effectParameter = new ParticleEffectLoader.ParticleEffectParameter();
        effectParameter.imagesDir = Gdx.files.internal("imagenes");

        assetManager.load("imagenes/fondo_juego.png", Texture.class);
        assetManager.load("imagenes/gragea.png", Texture.class);
        assetManager.load("imagenes/super_gragea.png", Texture.class);
        assetManager.load("imagenes/gragea_brillo.png", Texture.class);
        assetManager.load("imagenes/musica_on.png", Texture.class);
        assetManager.load("imagenes/musica_off.png", Texture.class);
        assetManager.load("imagenes/musica_click.png", Texture.class);
        assetManager.load("imagenes/btn_poder_mov_diagonal_on.png", Texture.class);
        assetManager.load("imagenes/btn_poder_mov_diagonal_off.png", Texture.class);
        assetManager.load("imagenes/btn_poder_mov_diagonal_click.png", Texture.class);
        assetManager.load("imagenes/fin_btn_fondo.png", Texture.class);
        assetManager.load("fuentes/texto_bits.fnt", BitmapFont.class);
        assetManager.load("sonidos/musica_fondo_dif1.mp3", Music.class);
        assetManager.load("sonidos/musica_fondo_dif2.mp3", Music.class);
        assetManager.load("sonidos/musica_fondo_dif3.mp3", Music.class);
        assetManager.load("sonidos/musica_fondo_lvl_1_10.mp3", Music.class);
        assetManager.load("sonidos/musica_fondo_lvl_10_20.mp3", Music.class);
        assetManager.load("sonidos/musica_fondo_lvl_20_30.mp3", Music.class);
        assetManager.load("sonidos/musica_fondo_lvl_30_40.mp3", Music.class);
        assetManager.load("sonidos/musica_fondo_lvl_40_50.mp3", Music.class);
        assetManager.load("sonidos/explosion.mp3", Sound.class);
        assetManager.load("sonidos/super_explosion.mp3", Sound.class);
        assetManager.load("efectos/explosion.effect", ParticleEffect.class, effectParameter);
        assetManager.load("efectos/brillante.effect", ParticleEffect.class, effectParameter);
        assetManager.load("strings/strings", I18NBundle.class);
        assetManager.finishLoading();
        txtFondo = assetManager.get("imagenes/fondo_juego.png");
        txtGragea = assetManager.get("imagenes/gragea.png");
        txtSuperGragea = assetManager.get("imagenes/super_gragea.png");
        txtGrageaBrillo = assetManager.get("imagenes/gragea_brillo.png");
        txtBtnMusicaOn = assetManager.get("imagenes/musica_on.png");
        txtBtnMusicaOff = assetManager.get("imagenes/musica_off.png");
        txtBtnMusicaClick = assetManager.get("imagenes/musica_click.png");
        txtBtnPoderMovDiagonalOn = assetManager.get("imagenes/btn_poder_mov_diagonal_on.png");
        txtBtnPoderMovDiagonalOff = assetManager.get("imagenes/btn_poder_mov_diagonal_off.png");
        txtBtnPoderMovDiagonalClick = assetManager.get("imagenes/btn_poder_mov_diagonal_click.png");
        txtFinJuegoFondo = assetManager.get("imagenes/fin_btn_fondo.png");
        fntFuenteBase = assetManager.get("fuentes/texto_bits.fnt");
        mscMusicaFondoDif1 = assetManager.get("sonidos/musica_fondo_dif1.mp3");
        mscMusicaFondoDif2 = assetManager.get("sonidos/musica_fondo_dif2.mp3");
        mscMusicaFondoDif3 = assetManager.get("sonidos/musica_fondo_dif3.mp3");
        mscMusicaFondoNiveles1 = assetManager.get("sonidos/musica_fondo_lvl_1_10.mp3");
        mscMusicaFondoNiveles2 = assetManager.get("sonidos/musica_fondo_lvl_10_20.mp3");
        mscMusicaFondoNiveles3 = assetManager.get("sonidos/musica_fondo_lvl_20_30.mp3");
        mscMusicaFondoNiveles4 = assetManager.get("sonidos/musica_fondo_lvl_30_40.mp3");
        mscMusicaFondoNiveles5 = assetManager.get("sonidos/musica_fondo_lvl_40_50.mp3");
        sndExplosion = assetManager.get("sonidos/explosion.mp3");
        sndSuperExplosion = assetManager.get("sonidos/super_explosion.mp3");
        parEfcExplosion = assetManager.get("efectos/explosion.effect");
        parEfcBrillante = assetManager.get("efectos/brillante.effect");
        strings = assetManager.get("strings/strings");
    }

    public void limpiarPosGrageas() {
        primerGrageaX = -1;
        primerGrageaY = -1;
        segundaGrageaX = -1;
        segundaGrageaY = -1;
    }

    public boolean verificarAdyacentes() {
        return (primerGrageaX != -1 && primerGrageaY != -1 && segundaGrageaX != -1 && segundaGrageaY != -1) &&
                (segundaGrageaX == primerGrageaX && ((segundaGrageaY == primerGrageaY - 1) || (segundaGrageaY == primerGrageaY + 1)))
                || (segundaGrageaY == primerGrageaY && ((segundaGrageaX == primerGrageaX - 1) || (segundaGrageaX == primerGrageaX + 1)));
    }

    public boolean verificarDiagonales() {
        return ((primerGrageaX != -1 && primerGrageaY != -1 && segundaGrageaX != -1 && segundaGrageaY != -1) &&
                (segundaGrageaX == primerGrageaX - 1 || segundaGrageaX == primerGrageaX + 1) &&
                (segundaGrageaY == primerGrageaY - 1 || segundaGrageaY == primerGrageaY + 1));
    }

    public boolean isHayGrageaSeleccionada() {
        return hayGrageaSeleccionada;
    }

    public void setHayGrageaSeleccionada(boolean hayGrageaSeleccionada) {
        this.hayGrageaSeleccionada = hayGrageaSeleccionada;
    }

    public CyclicBarrier getBarrierRespuestaVisual() {
        return barrierRespuestaVisual;
    }

    public void setBarrierRespuestaVisual(CyclicBarrier barrierRespuestaVisual) {
        this.barrierRespuestaVisual = barrierRespuestaVisual;
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

    public boolean isInputGrageas() {
        return inputGrageas;
    }

    public void setInputGrageas(boolean inputGrageas) {
        this.inputGrageas = inputGrageas;
    }

    public GrageaVisual[][] getMatrizGrageasVisuales() {
        return matrizGrageasVisuales;
    }

    public boolean isInputMenus() {
        return inputMenus;
    }

    public void setInputMenus(boolean inputMenus) {
        this.inputMenus = inputMenus;
    }

    public int getAnimacionesEjecutando() {
        return animacionesEjecutando;
    }

    public void setAnimacionesEjecutando(int animacionesEjecutando) {
        this.animacionesEjecutando = animacionesEjecutando;
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

    public void actualizarUltimaJugada(Long valor) {
        ultimaJugada = valor;
    }
}
