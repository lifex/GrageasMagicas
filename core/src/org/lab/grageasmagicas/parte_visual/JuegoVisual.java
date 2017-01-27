package org.lab.grageasmagicas.parte_visual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Music;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.lab.estructuras.Point;
import org.lab.grageasmagicas.parte_logica.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;

import static java.lang.Thread.sleep;


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
    private boolean inputGrageas;
    private boolean inputMenus;
    private boolean tableroListo;
    private GrageaVisual[][] matrizGrageasVisuales;
    private CyclicBarrier barrierRespuestaVisual;
    private float[][] matrizPosGrageaX;
    private float[][] matrizPosGrageaY;
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
    //actors
    private Table tblTablero;
    private TextButton btnPuntaje;
    private TextButton btnVolver;
    private TextButton btnSinMovimiento;
    private ImageButton btnMusica;
    private Image imgFondo;
    //assets
    private Texture txtFondo;
    private Texture txtGragea;
    private Texture txtBtnMusicaOn;
    private Texture txtBtnMusicaOff;
    private Texture txtBtnMusicaClick;
    private BitmapFont fntFuenteBase;
    private Music mscMusicaFondo;
    private ParticleEffect parEfcExplosion;
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
        this.animacionesEjecutando = 0;
        this.tableroListo = false;

        cargarAssets();

        mscMusicaFondo.setLooping(true);
        mscMusicaFondo.setVolume(0.25f);
        mscMusicaFondo.play();

        escena = new Stage(vista);
        Gdx.input.setInputProcessor(escena);

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
            btnPuntaje.setText((int) juegoLogico.getPuntaje() + "");
            btnPuntaje.setWidth(btnPuntaje.getPrefWidth());
            btnPuntaje.setHeight(btnPuntaje.getPrefHeight());
            btnPuntaje.setPosition(50, altoCamara - btnPuntaje.getHeight() - 25);

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

        tblTablero = new Table();
        escena.addActor(tblTablero);

        TextButton.TextButtonStyle btnStlPuntaje = new TextButton.TextButtonStyle();
        btnStlPuntaje.font = fntFuenteBase;
        btnStlPuntaje.fontColor = Color.GOLD;
        btnPuntaje = new TextButton((int) juegoLogico.getPuntaje() + "", btnStlPuntaje);
        btnPuntaje.getLabel().setFontScale(2, 2);
        btnPuntaje.setWidth(btnPuntaje.getPrefWidth());
        btnPuntaje.setHeight(btnPuntaje.getPrefHeight());
        btnPuntaje.setPosition(50, altoCamara - btnPuntaje.getHeight() - 25);
        escena.addActor(btnPuntaje);

        TextButton.TextButtonStyle btnStlVolver = new TextButton.TextButtonStyle();
        btnStlVolver.font = fntFuenteBase;
        btnVolver = new TextButton("MENU", btnStlVolver);
        btnVolver.getLabel().setFontScale(2, 2);
        btnVolver.setWidth(btnVolver.getPrefWidth());
        btnVolver.setHeight(btnVolver.getPrefHeight());
        btnVolver.setPosition(anchoCamara - btnVolver.getWidth() - 50, altoCamara - btnVolver.getHeight() - 25);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (isInputMenus()) {
                        MenuPrincipal menuPrincipal = new MenuPrincipal(adminPantalla);
                        juegoLogico.terminar();

                        barrierRespuestaVisual.await();

                        //dispose();
                        adminPantalla.setScreen(menuPrincipal);
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
        btnMusica.setDisabled(true);
        btnMusica.setPosition(anchoCamara - btnMusica.getWidth() - 75, altoCamara - btnMusica.getHeight() - btnVolver.getHeight() - 50);
        btnMusica.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isInputMenus()) {
                    btnMusica.setDisabled(false);
                    if (mscMusicaFondo.isPlaying()) {
                        mscMusicaFondo.pause();
                        btnMusica.setChecked(true);
                    } else {
                        mscMusicaFondo.play();
                        btnMusica.setChecked(false);
                    }
                    btnMusica.setDisabled(true);
                }
            }
        });
        escena.addActor(btnMusica);

        TextButton.TextButtonStyle btnStlSinMoviminto = new TextButton.TextButtonStyle();
        btnStlSinMoviminto.font = fntFuenteBase;
        btnStlSinMoviminto.fontColor = Color.RED;
        btnSinMovimiento = new TextButton("NO HAY MOVIMIENTOS", btnStlSinMoviminto);
        btnSinMovimiento.setPosition(anchoCamara / 2 - btnSinMovimiento.getWidth() / 2, altoCamara - 50);
        btnSinMovimiento.setVisible(false);
        btnSinMovimiento.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    btnSinMovimiento.setVisible(false);
                    barrierRespuestaVisual.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        escena.addActor(btnSinMovimiento);

        tblTablero.row();
        for (int i = 0; i < cantFilas; i++) {
            for (int j = 0; j < cantColumnas; j++) {
                matrizGrageasVisuales[i][j] = new GrageaVisual(matrizGrageasLogica[i][j].getTipo(), txtGragea);
                matrizGrageasVisuales[i][j].addListener(new GrageaVisualListener(matrizGrageasVisuales[i][j], this, i, j));
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
        if (juegoLogico.getPrimerGrageaX() != -1) {
            GrageaVisual priGragea = matrizGrageasVisuales[juegoLogico.getPrimerGrageaX()][juegoLogico.getPrimerGrageaY()];
            GrageaVisual segGragea = matrizGrageasVisuales[juegoLogico.getSegundaGrageaX()][juegoLogico.getSegundaGrageaY()];
            GrageaVisualListener priGrageaListener = (GrageaVisualListener) (priGragea.getListeners().get(0));
            GrageaVisualListener segGrageaListener = (GrageaVisualListener) (segGragea.getListeners().get(0));
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
                sleep(500);
            }
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
                sleep(500);
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
                        System.out.println(matrizGrageasVisuales[i][j].getX()+","+matrizGrageasVisuales[i][j].getY());
                        poolEfcExplosion.setPosition(
                                matrizGrageasVisuales[i][j].getX()+matrizGrageasVisuales[i][j].getWidth()/2,
                                matrizGrageasVisuales[i][j].getY()+matrizGrageasVisuales[i][j].getHeight()/2);
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
            }
            //Sound sCombinacion = Gdx.audio.newSound(Gdx.files.internal("combinacion.mp3"));
            if (animacionesEjecutando > 0) {
                dormir();
                sleep(500);
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
     * Permite sincronizar las animaciones con el juegoVisual, haciendo que la ultima animacion en
     * terminar lo despierte
     */
    synchronized public void animacionTermina() {
        animacionesEjecutando--;
        System.out.println(animacionesEjecutando);
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
            batch.end();
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
        txtBtnMusicaOn.dispose();
        txtBtnMusicaOff.dispose();
        txtBtnMusicaClick.dispose();
        fntFuenteBase.dispose();
        mscMusicaFondo.dispose();
        parEfcExplosion.dispose();
        escena.dispose();
        assetManager.unload("imagenes/fondogolosinas.png");
        assetManager.unload("imagenes/gragea.png");
        assetManager.unload("imagenes/musica_on.png");
        assetManager.unload("imagenes/musica_off.png");
        assetManager.unload("imagenes/musica_click.png");
        assetManager.unload("fuentes/texto_bits.fnt");
        assetManager.unload("sonidos/musica_fondo.mp3");
        assetManager.unload("efectos/explosion.effect");
    }

    public void cargarAssets() {
        //loader para efectos de particulas
        ParticleEffectLoader.ParticleEffectParameter effectParameter = new ParticleEffectLoader.ParticleEffectParameter();
        effectParameter.imagesDir = Gdx.files.internal("imagenes");

        assetManager.load("imagenes/fondogolosinas.png", Texture.class);
        assetManager.load("imagenes/gragea.png", Texture.class);
        assetManager.load("imagenes/musica_on.png", Texture.class);
        assetManager.load("imagenes/musica_off.png", Texture.class);
        assetManager.load("imagenes/musica_click.png", Texture.class);
        assetManager.load("fuentes/texto_bits.fnt", BitmapFont.class);
        assetManager.load("sonidos/musica_fondo.mp3", Music.class);
        assetManager.load("efectos/explosion.effect", ParticleEffect.class, effectParameter);
        assetManager.finishLoading();
        txtFondo = assetManager.get("imagenes/fondogolosinas.png");
        txtGragea = assetManager.get("imagenes/gragea.png");
        txtBtnMusicaOn = assetManager.get("imagenes/musica_on.png");
        txtBtnMusicaOff = assetManager.get("imagenes/musica_off.png");
        txtBtnMusicaClick = assetManager.get("imagenes/musica_click.png");
        fntFuenteBase = assetManager.get("fuentes/texto_bits.fnt");
        mscMusicaFondo = assetManager.get("sonidos/musica_fondo.mp3");
        parEfcExplosion = assetManager.get("efectos/explosion.effect");
    }

    public void limpiarPosGrageas() {
        primerGrageaX = -1;
        primerGrageaY = -1;
        segundaGrageaX = -1;
        segundaGrageaY = -1;
    }

    public boolean verificarAdyacentes() {
        return (segundaGrageaX == primerGrageaX && ((segundaGrageaY == primerGrageaY - 1) || (segundaGrageaY == primerGrageaY + 1)))
                || (segundaGrageaY == primerGrageaY && ((segundaGrageaX == primerGrageaX - 1) || (segundaGrageaX == primerGrageaX + 1)));
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
}
