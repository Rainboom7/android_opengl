package com.example.ghat_gpt_renderer.lightning;

//import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MyOpes3Activity extends Activity {
    // СЃРѕР·РґР°РґРёРј СЃСЃС‹Р»РєСѓ РЅР° СЌРєР·РµРјРїР»СЏСЂ РЅР°С€РµРіРѕ РєР»Р°СЃСЃР° MyClassSurfaceView
    private MyClassSurfaceView mGLSurfaceView;

    // РїРµСЂРµРѕРїСЂРµРґРµР»РёРј РјРµС‚РѕРґ
    // onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//СЃРѕР·РґР°РґРёРј СЌРєР·РµРјРїР»СЏСЂ РЅР°С€РµРіРѕ РєР»Р°СЃСЃР° MyClassSurfaceView
        mGLSurfaceView = new MyClassSurfaceView(this);

//РІС‹Р·РѕРІРµРј СЌРєР·РµРјРїР»СЏСЂ РЅР°С€РµРіРѕ РєР»Р°СЃСЃР° MyClassSurfaceView
        setContentView(mGLSurfaceView);
// РЅР° СЌРєСЂР°РЅРµ РїРѕСЏРІРёС‚СЃСЏ РїРѕРІРµСЂС…РЅРѕСЃС‚СЊ РґР»СЏ СЂРёСЃРѕРІР°РЅРёСЏ РІ OpenGl ES
    }
    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }
}