package com.example.ghat_gpt_renderer.my_scene_lightning_2d.lightning;

import android.content.Context;
import android.opengl.GLSurfaceView;

//Опишем наш класс MyClassSurfaceView расширяющий GLSurfaceView
public class MyClassSurfaceView extends GLSurfaceView{
    //создадим ссылку для хранения экземпляра нашего класса рендерера
    private MyClassRenderer renderer;

    // конструктор
    public MyClassSurfaceView(Context context) {
        // вызовем конструктор родительского класса GLSurfaceView
        super(context);
        setEGLContextClientVersion(2);
        // создадим экземпляр нашего класса MyClassRenderer
        renderer = new MyClassRenderer(context);
        // запускаем рендерер
        setRenderer(renderer);
        // установим режим циклического запуска метода onDrawFrame
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        // при этом запускается отдельный поток
        // в котором циклически вызывается метод onDrawFrame
        // т.е. бесконечно происходит перерисовка кадров
    }
}

