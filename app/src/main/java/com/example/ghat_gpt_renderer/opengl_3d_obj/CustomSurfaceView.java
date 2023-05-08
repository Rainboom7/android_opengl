package com.example.ghat_gpt_renderer.opengl_3d_obj;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class CustomSurfaceView extends GLSurfaceView {

    private CameraRendererMy renderer;


    public CustomSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);

        renderer = new CameraRendererMy(context);

        setRenderer(renderer);

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void applyRotation(float newX, float newY) {
        renderer.applyRotation(newX, newY);
    }
}
