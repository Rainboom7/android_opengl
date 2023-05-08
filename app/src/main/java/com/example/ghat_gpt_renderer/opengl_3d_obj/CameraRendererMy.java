package com.example.ghat_gpt_renderer.opengl_3d_obj;

import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.example.ghat_gpt_renderer.opengl_3d_obj.util.ObjectRenderer;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRendererMy implements Renderer {

    private float xLightPosition, yLightPosition, zLightPosition;
    float angleX = 45.0f;
    float angleY = 90.0f;
    float angleZ = 0.0f;

    private Context context;

    private float[] DEFAULT_COLOR = new float[]{139.0f, 240.0f, 74.0f, 255.0f};
    // матрица модели
    private float[] mModelMatrix = new float[16];
    // видовая матрица
    private float[] mViewMatrix = new float[16];
    // модельновидовая матрица
    private float[] mMVPMatrix = new float[16];
    // проекционная матрица
    private float[] mProjectionMatrix = new float[16];
    private ObjectRenderer virtualObject = new ObjectRenderer();
    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 4;
    private final int mBytesPerFloat = 4;

    private final int mStrideBytes = 7 * mBytesPerFloat;

    /**
     * Offset of the color data.
     */
    private final int mColorOffset = 3;

    /**
     * Size of the color data in elements.
     */
    private final int mColorDataSize = 4;
    private final int mPositionOffset = 0;

    private float mCubeRotation =0.6f;

    // переменная матрицы трансформации
    private int mMVPMatrixHandle;
    // переменная для model position данных
    private int mPositionHandle;

    private Cube mCube;

    private int mColorHandle;

    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private ShortBuffer indexBuffer;

    private float x;

    private final float[] mRotationMatrixX = new float[16];
    private final float[] mRotationMatrixY = new float[16];
    private final float[] mRotationMatrixZ = new float[16];

    private final float[] mFinalMVPMatrix= new float[16];

    private int programHandle;

    public CameraRendererMy(Context context) {
        // Define points for equilateral triangles.
        this.context = context;
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //включаем отсечение невидимых граней
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        //включаем сглаживание текстур, это пригодится в будущем
        GLES20.glHint(
                GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);

        glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        //координаты точечного источника света
        xLightPosition = 0.3f;
        yLightPosition = 0.2f;
        zLightPosition = 0.5f;

        // Позиция камеры за объектом
        final float eyeX = 0.0f;
        final float eyeY = 0.2f;
        final float eyeZ = 1.2f;

        // Определяем напрвление камеры
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Устанавливаем позицию up-вектора камеры. This is where our head would be pointing were we holding the camera.
        final float upX = 1.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        //установление камеры (матрицы просмотра)
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"       // A constant representing the combined model/view/projection matrix.
                        + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.

                        + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.

                        + "void main()                    \n"     // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"     // Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix * a_Position; \n"  // gl_Position is a special variable used to store the final position.

                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"       // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"     // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"     // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
                        + "}                              \n";

        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
         programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            //  Привязка атрибутов
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

// Устанавливаем программные переменные в переменныен шейдора uniform and atribut
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);


        //Отрисовка моделей
        mCube = new Cube();

        try {
            virtualObject.createOnGlThread(context, "models/andy.obj", "models/andy.png");
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    // Set the OpenGL viewport to fill the entire surface.
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);

        // Создаем новую перспективнро-поекционную матрицу. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

    }

    @Override
    // Clear the rendering surface.
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);


        if (x <= 1) {
            x = (float) (x + 0.001);
        } else {
            x = 0;
        }

        Matrix.setRotateM(mRotationMatrixX, 0, mCubeRotation, 1.0f, 0, 0);
        Matrix.setRotateM(mRotationMatrixY, 0, mCubeRotation, 0, 1.0f, 0);
        Matrix.setRotateM(mRotationMatrixZ, 0, mCubeRotation, 0, 0, 1.0f);
        float[] rotationMatrix = new float[16];
        Matrix.multiplyMM(rotationMatrix, 0, mRotationMatrixX, 0, mRotationMatrixY, 0);
        Matrix.multiplyMM(rotationMatrix, 0, rotationMatrix, 0, mRotationMatrixZ, 0);
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        scaleMatrix[0] = 1;
        scaleMatrix[5] = 1;
        scaleMatrix[10] = 1;

        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(mFinalMVPMatrix, 0, rotationMatrix, 0, scaleMatrix, 0);

        virtualObject.draw(mViewMatrix, mProjectionMatrix, new float[]{xLightPosition, yLightPosition, zLightPosition, 0.7f},
                DEFAULT_COLOR);

        mCube.draw(mFinalMVPMatrix);
    }

    public void applyRotation(float newX, float newY) {

// Create rotation matrices
        float[] rotationMatrixX = new float[16];
        float[] rotationMatrixY = new float[16];
        float[] rotationMatrixZ = new float[16];

        Matrix.setIdentityM(rotationMatrixX, 0);
        Matrix.setIdentityM(rotationMatrixY, 0);
        Matrix.setIdentityM(rotationMatrixZ, 0);

        Matrix.rotateM(rotationMatrixX, 0, newX, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(rotationMatrixY, 0, newY, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(rotationMatrixZ, 0, angleZ, 0.0f, 0.0f, 1.0f);

// Combine rotation matrices
        float[] rotationMatrix = new float[16];
        Matrix.multiplyMM(rotationMatrix, 0, rotationMatrixX, 0, rotationMatrixY, 0);
        Matrix.multiplyMM(rotationMatrix, 0, rotationMatrix, 0, rotationMatrixZ, 0);

        virtualObject.updateModelMatrix(rotationMatrix, 2);

        mCubeRotation += 0.6f;

    }


}
