package org.lab.grageasmagicas.parte_visual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.Viewport;


public class MenuOpciones implements Screen {

    //visual
    private int anchoCamara;
    private int altoCamara;
    //administradores
    private AssetManager assetManager;
    private AdministradorPantalla adminPantalla;
    private Viewport vista;
    private Stage escena;
    private I18NBundle strings;
    //actors
    private TextButton btnVolver;
    private TextButton btnComoJugar;
    private TextButton btnSonidoOnOf;
    private TextButton btnVibracion;
    private Label lblOpciones;
    private Table tblContenido;
    private Image imgFondo;
    //assets
    private Texture txtFondo;
    private BitmapFont fntFuenteBase;

    public MenuOpciones(AdministradorPantalla adminPantalla) {
        this.adminPantalla = adminPantalla;
        this.anchoCamara = adminPantalla.getAnchoCamara();
        this.altoCamara = adminPantalla.getAltoCamara();
        this.vista = adminPantalla.getVista();
        this.assetManager = adminPantalla.getAssetManager();

        cargarAssets();

        escena = new Stage(vista);
        Gdx.input.setInputProcessor(escena);
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void show() {
        imgFondo = new Image(txtFondo);
        imgFondo.setScale(anchoCamara / imgFondo.getWidth(), altoCamara / imgFondo.getHeight());
        escena.addActor(imgFondo);

        Label.LabelStyle lblStlOpciones = new Label.LabelStyle(fntFuenteBase, Color.GOLD);

        lblOpciones = new Label(strings.get("btn_opciones"), lblStlOpciones);
        lblOpciones.setFontScale(2f, 2f);
        lblOpciones.setWrap(true);
        lblOpciones.setAlignment(1);

        TextButton.TextButtonStyle btnStlComoJugar = new TextButton.TextButtonStyle();
        btnStlComoJugar.font = fntFuenteBase;
        btnStlComoJugar.fontColor = Color.GREEN;

        btnComoJugar = new TextButton(strings.get("btn_como_jugar"), btnStlComoJugar);
        btnComoJugar.getLabel().setFontScale(1.5f, 1.5f);
        btnComoJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuComoJugar()));
            }
        });

        TextButton.TextButtonStyle btnStlSonidoOnOf = new TextButton.TextButtonStyle();
        btnStlSonidoOnOf.font = fntFuenteBase;

        btnSonidoOnOf = new TextButton(strings.get("btn_sonido"), btnStlSonidoOnOf);
        btnSonidoOnOf.getLabel().setFontScale(1.5f, 1.5f);
        if (adminPantalla.getInterfazDb().consultarOpcionSonido(adminPantalla.getIdUsuario())) {
            btnSonidoOnOf.setText(strings.get("btn_sonido") + ": " + strings.get("encendido"));
        } else {
            btnSonidoOnOf.setText(strings.get("btn_sonido") + ": " + strings.get("apagado"));
        }
        btnSonidoOnOf.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (adminPantalla.getInterfazDb().consultarOpcionSonido(adminPantalla.getIdUsuario())) {
                    btnSonidoOnOf.setText(strings.get("btn_sonido") + ": " + strings.get("apagado"));
                    adminPantalla.getInterfazDb().setOpcionSonido(adminPantalla.getIdUsuario(), false);
                } else {
                    btnSonidoOnOf.setText(strings.get("btn_sonido") + ": " + strings.get("encendido"));
                    adminPantalla.getInterfazDb().setOpcionSonido(adminPantalla.getIdUsuario(), true);
                }
            }
        });

        TextButton.TextButtonStyle btnStlVibracion = new TextButton.TextButtonStyle();
        btnStlVibracion.font = fntFuenteBase;

        btnVibracion = new TextButton(strings.get("btn_vibrar"), btnStlVibracion);
        btnVibracion.getLabel().setFontScale(1.5f, 1.5f);
        if (adminPantalla.getInterfazDb().consultarOpcionVibracion(adminPantalla.getIdUsuario())) {
            btnVibracion.setText(strings.get("btn_vibrar") + ": " + strings.get("encendido"));
        } else {
            btnVibracion.setText(strings.get("btn_vibrar") + ": " + strings.get("apagado"));
        }
        btnVibracion.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (adminPantalla.getInterfazDb().consultarOpcionVibracion(adminPantalla.getIdUsuario())) {
                    btnVibracion.setText(strings.get("btn_vibrar") + ": " + strings.get("apagado"));
                    adminPantalla.getInterfazDb().setOpcionVibracion(adminPantalla.getIdUsuario(), false);
                } else {
                    btnVibracion.setText(strings.get("btn_vibrar") + ": " + strings.get("encendido"));
                    adminPantalla.getInterfazDb().setOpcionVibracion(adminPantalla.getIdUsuario(), true);
                    Gdx.input.vibrate(200);
                }
            }
        });

        tblContenido = new Table();
        tblContenido.row();
        tblContenido.add(lblOpciones).pad(50f);
        tblContenido.row();
        tblContenido.add(btnComoJugar).padBottom(25f);
        tblContenido.row();
        tblContenido.add(btnSonidoOnOf).padBottom(25f);
        tblContenido.row();
        tblContenido.add(btnVibracion).padBottom(25f);
        //tblContenido.debug();
        tblContenido.pack();
        tblContenido.setPosition(anchoCamara / 2 - tblContenido.getWidth() / 2, altoCamara - tblContenido.getHeight());
        escena.addActor(tblContenido);

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
                adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuPrincipal()));
            }
        });
        escena.addActor(btnVolver);
    }

    @Override
    public void resize(int width, int height) {
        vista.update(width, height);
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
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        escena.act(delta);
        escena.setViewport(vista);
        escena.draw();
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)){
            adminPantalla.setScreen(new PantallaIntermedia(adminPantalla, adminPantalla.getMenuPrincipal()));
        }
    }

    @Override
    public void dispose() {
        txtFondo.dispose();
        fntFuenteBase.dispose();
        escena.dispose();
        //assetManager.clear();
        assetManager.unload("imagenes/fondo_tablero.png");
        assetManager.unload("fuentes/texto_bits.fnt");
        assetManager.unload("strings/strings");
    }

    private void cargarAssets() {
        assetManager.load("imagenes/fondo_tablero.png", Texture.class);
        assetManager.load("fuentes/texto_bits.fnt", BitmapFont.class);
        assetManager.load("strings/strings", I18NBundle.class);
        assetManager.finishLoading();
        txtFondo = assetManager.get("imagenes/fondo_tablero.png");
        fntFuenteBase = assetManager.get("fuentes/texto_bits.fnt");
        strings = assetManager.get("strings/strings");
    }


}
