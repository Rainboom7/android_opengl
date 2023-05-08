package com.example.ghat_gpt_renderer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;

import com.example.ghat_gpt_renderer.opengl_3d_obj.CustomSurfaceView;

public class MainActivity extends AppCompatActivity {
    // Define rotation angles in degrees
    private float angleX = 0.0f;
    private float angleY = 0.0f;

    // Variables to keep track of touch events
    private float previousX;
    private float previousY;
    private CustomSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new CustomSurfaceView(this);
        setContentView(mGLView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // Calculate rotation angles based on touch movement
                float deltaX = currentX - previousX;
                float deltaY = currentY - previousY;
                angleX += deltaY / 10.0f;
                angleY += deltaX / 10.0f;
                mGLView.applyRotation(angleX, angleY);
                break;
        }

        // Save previous touch position
        previousX = currentX;
        previousY = currentY;

        return true;
    }
}