package com.example.ghat_gpt_renderer.light2;

import android.opengl.GLSurfaceView;

import android.content.Context;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;

import com.example.ghat_gpt_renderer.lightning.Shader;

public class OpenGLRenderer implements GLSurfaceView.Renderer
{
    // интерфейс GLSurfaceView.Renderer содержит
    // три метода onDrawFrame, onSurfaceChanged, onSurfaceCreated
    // которые должны быть переопределены
    // текущий контекст
    private Context context;
    //координаты камеры
    private float xСamera, yCamera, zCamera;
    //координаты источника света
    private float xLightPosition, yLightPosition, zLightPosition;
    //матрицы
    private float[] modelMatrix;
    private float[] viewMatrix;
    private float[] modelViewMatrix;
    private float[] projectionMatrix;
    private float[] modelViewProjectionMatrix;
    //буфер для координат вершин
    private FloatBuffer vertexBuffer;
    //буфер для нормалей вершин
    private FloatBuffer normalBuffer;
    //буфер для цветов вершин
    private FloatBuffer colorBuffer;
    //шейдерный объект
    private com.example.ghat_gpt_renderer.lightning.Shader mShader;

    //конструктор
    public OpenGLRenderer(Context context) {
        // запомним контекст
        // он нам понадобится в будущем для загрузки текстур
        this.context=context;
        //координаты точечного источника света
        xLightPosition=0;
        yLightPosition=0.6f;
        zLightPosition=0;
        //матрицы
        modelMatrix=new float[16];
        viewMatrix=new float[16];
        modelViewMatrix=new float[16];
        projectionMatrix=new float[16];
        modelViewProjectionMatrix=new float[16];
        //мы не будем двигать объекты
        //поэтому сбрасываем модельную матрицу на единичную
        Matrix.setIdentityM(modelMatrix, 0);
        //координаты камеры
        xСamera=0.6f;
        yCamera=3.4f;
        zCamera=3f;
        //пусть камера смотрит на начало координат
        //и верх у камеры будет вдоль оси Y
        //зная координаты камеры получаем матрицу вида
        Matrix.setLookAtM(
                viewMatrix, 0, xСamera, yCamera, zCamera, 0, 0, 0, 0, 1, 0);
        // умножая матрицу вида на матрицу модели
        // получаем матрицу модели-вида
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        //координаты вершины 1
        float x1=-2;
        float y1=0;
        float z1=-2;
        //координаты вершины 2
        float x2=-2;
        float y2=0;
        float z2=2;
        //координаты вершины 3
        float x3=2;
        float y3=0;
        float z3=-2;
        //координаты вершины 4
        float x4=2;
        float y4=0;
        float z4=2;
        //запишем координаты всех вершин в единый массив
        float vertexArray [] = {x1,y1,z1, x2,y2,z2, x3,y3,z3, x4,y4,z4};
        //создадим буфер для хранения координат вершин
        ByteBuffer bvertex = ByteBuffer.allocateDirect(vertexArray.length*4);
        bvertex.order(ByteOrder.nativeOrder());
        vertexBuffer = bvertex.asFloatBuffer();
        vertexBuffer.position(0);
        //перепишем координаты вершин из массива в буфер
        vertexBuffer.put(vertexArray);
        vertexBuffer.position(0);
        //вектор нормали перпендикулярен плоскости квадрата
        //и направлен вдоль оси Y
        float nx=0;
        float ny=1;
        float nz=0;
        //нормаль одинакова для всех вершин квадрата,
        //поэтому переписываем координаты вектора нормали в массив 4 раза
        float normalArray [] ={nx, ny, nz,   nx, ny, nz,   nx, ny, nz,   nx, ny, nz};
        //создадим буфер для хранения координат векторов нормали
        ByteBuffer bnormal = ByteBuffer.allocateDirect(normalArray.length*4);
        bnormal.order(ByteOrder.nativeOrder());
        normalBuffer = bnormal.asFloatBuffer();
        normalBuffer.position(0);
        //перепишем координаты нормалей из массива в буфер
        normalBuffer.put(normalArray);
        normalBuffer.position(0);
        //разукрасим вершины квадрата, зададим цвета для вершин
        //цвет первой вершины - красный
        float red1=1;
        float green1=0;
        float blue1=0;
        //цвет второй вершины - зеленый
        float red2=0;
        float green2=1;
        float blue2=0;
        //цвет третьей вершины - синий
        float red3=0;
        float green3=0;
        float blue3=1;
        //цвет четвертой вершины - желтый
        float red4=1;
        float green4=1;
        float blue4=0;
        //перепишем цвета вершин в массив
        //четвертый компонент цвета (альфу) примем равным единице
        float colorArray [] = {
                red1, green1, blue1, 1,
                red2, green2, blue2, 1,
                red3, green3, blue3, 1,
                red4, green4, blue4, 1,
        };
        //создадим буфер для хранения цветов вершин
        ByteBuffer bcolor = ByteBuffer.allocateDirect(colorArray.length*4);
        bcolor.order(ByteOrder.nativeOrder());
        colorBuffer = bcolor.asFloatBuffer();
        colorBuffer.position(0);
        //перепишем цвета вершин из массива в буфер
        colorBuffer.put(colorArray);
        colorBuffer.position(0);
    }//конец конструктора

    //метод, который срабатывает при изменении размеров экрана
    //в нем мы получим матрицу проекции и матрицу модели-вида-проекции
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // устанавливаем glViewport
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float k=0.055f;
        float left = -k*ratio;
        float right = k*ratio;
        float bottom = -k;
        float top = k;
        float near = 0.1f;
        float far = 10.0f;
        // получаем матрицу проекции
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
        // матрица проекции изменилась,
        // поэтому нужно пересчитать матрицу модели-вида-проекции
        Matrix.multiplyMM(
                modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    //метод, который срабатывает при создании экрана
    //здесь мы создаем шейдерный объект
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //включаем тест глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //включаем отсечение невидимых граней
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        //включаем сглаживание текстур, это пригодится в будущем
        GLES20.glHint(
                GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
        //записываем код вершинного шейдера в виде строки
        String vertexShaderCode=
                "uniform mat4 u_modelViewProjectionMatrix;"+
                        "attribute vec3 a_vertex;"+
                        "attribute vec3 a_normal;"+
                        "attribute vec4 a_color;"+
                        "varying vec3 v_vertex;"+
                        "varying vec3 v_normal;"+
                        "varying vec4 v_color;"+
                        "void main() {"+
                        "v_vertex=a_vertex;"+
                        "vec3 n_normal=normalize(a_normal);"+
                        "v_normal=n_normal;"+
                        "v_color=a_color;"+
                        "gl_Position = u_modelViewProjectionMatrix * vec4(a_vertex,1.0);"+
                        "}";
        //записываем код фрагментного шейдера в виде строки
        String fragmentShaderCode=
                "precision mediump float;"+
                        "uniform vec3 u_camera;"+
                        "uniform vec3 u_lightPosition;"+
                        "varying vec3 v_vertex;"+
                        "varying vec3 v_normal;"+
                        "varying vec4 v_color;"+
                        "void main() {"+
                        "vec3 n_normal=normalize(v_normal);"+
                        "vec3 lightvector = normalize(u_lightPosition - v_vertex);"+
                        "vec3 lookvector = normalize(u_camera - v_vertex);"+
                        "float ambient=0.2;"+
                        "float k_diffuse=0.8;"+
                        "float k_specular=0.4;"+
                        "float diffuse = k_diffuse * max(dot(n_normal, lightvector), 0.0);"+
                        "vec3 reflectvector = reflect(-lightvector, n_normal);"+
                        "float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );"+
                        "vec4 one=vec4(1.0,1.0,1.0,1.0);"+
                        "gl_FragColor = (ambient+diffuse+specular)*one;"+
                        "}";
        //создадим шейдерный объект
        mShader=new Shader(vertexShaderCode, fragmentShaderCode);
        //свяжем буфер вершин с атрибутом a_vertex в вершинном шейдере
        mShader.linkVertexBuffer(vertexBuffer);
        //свяжем буфер нормалей с атрибутом a_normal в вершинном шейдере
        mShader.linkNormalBuffer(normalBuffer);
        //свяжем буфер цветов с атрибутом a_color в вершинном шейдере
        mShader.linkColorBuffer(colorBuffer);
        //связь атрибутов с буферами сохраняется до тех пор,
        //пока не будет уничтожен шейдерный объект
    }

    //метод, в котором выполняется рисование кадра
    public void onDrawFrame(GL10 unused) {
        //очищаем кадр
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //в отличие от атрибутов связь униформ с внешними параметрами
        //не сохраняется, поэтому перед рисованием каждого кадра
        //нужно связывать униформы заново
        //передаем в шейдерный объект матрицу модели-вида-проекции
        mShader.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        //передаем в шейдерный объект координаты камеры
        mShader.linkCamera(xСamera, yCamera, zCamera);
        //передаем в шейдерный объект координаты источника света
        mShader.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        //делаем шейдерную программу активной
        mShader.useProgram();
        //рисуем квадрат
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //последний аргумент в этой команде - это количество вершин =4
    }
}