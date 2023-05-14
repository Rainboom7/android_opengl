package com.example.ghat_gpt_renderer.my_scene_lightning_2d.lightning;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.ghat_gpt_renderer.R;
import com.example.ghat_gpt_renderer.light_texture.Texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MyClassRenderer implements GLSurfaceView.Renderer {


    // интерфейс GLSurfaceView.Renderer содержит
    // три метода onDrawFrame, onSurfaceChanged, onSurfaceCreated
    // которые должны быть переопределены
    // текущий контекст
    private Context context;
    //координаты камеры
    private float xCamera, yCamera, zCamera;
    //координаты источника света
    private float xLightPosition, yLightPosition, zLightPosition;
    //матрицы

    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] modelViewMatrix = new float[16];
    ;
    private float[] projectionMatrix = new float[16];
    ;
    private float[] modelViewProjectionMatrix = new float[16];
    ;
    //буфер для координат вершин
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer vertexBuffer1;
    private final FloatBuffer vertexBuffer4;
    //буфер для нормалей вершин
    private FloatBuffer normalBuffer;
    private FloatBuffer normalBuffer1;
    //буфер для цветов вершин
    private final FloatBuffer colorBuffer;
    private final FloatBuffer colorBuffer1;
    private final FloatBuffer colorBuffer4;
    //шейдерный объект
    private com.example.ghat_gpt_renderer.light_texture.Shader mShader;
    private Shader mShader1;
    private Texture texture;
    private Texture texture1;

    //конструктор
    public MyClassRenderer(Context context) {
        // запомним контекст
        // он нам понадобится в будущем для загрузки текстур
        this.context = context;

        Matrix.setIdentityM(modelMatrix, 0);

        //координаты точечного источника света
        xLightPosition = 0.3f;
        yLightPosition = 0.2f;
        zLightPosition = 0.5f;


        //мы не будем двигать объекты
        //поэтому сбрасываем модельную матрицу на единичную
        Matrix.setIdentityM(modelMatrix, 0);
        //координаты камеры
        xCamera = 0.0f;
        yCamera = 0.0f;
        zCamera = 3.0f;
        //пусть камера смотрит на начало координат
        //и верх у камеры будет вдоль оси Y
        //зная координаты камеры получаем матрицу вида
        Matrix.setLookAtM(
                viewMatrix, 0, xCamera, yCamera, zCamera, 0, 0, 0, 0, 1, 0);
        // умножая матрицу вида на матрицу модели
        // получаем матрицу модели-вида
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        //координаты вершины 1
        float x1 = -1;
        float y1 = -0.35f;
        float z1 = 0.0f;
        //координаты вершины 2
        float x2 = -1;
        float y2 = -1.5f;
        float z2 = 0.0f;
        //координаты вершины 3
        float x3 = 1;
        float y3 = -0.35f;
        float z3 = 0.0f;
        //координаты вершины 4
        float x4 = 1;
        float y4 = -1.5f;
        float z4 = 0.0f;
        //запишем координаты всех вершин в единый массив
        float vertexArray[] = {x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4};
        //coordinates for sky
        float vertexArray1[] = {
                -1.0f, 1.5f, 0.0f,
                -1.0f, -0.35f, 0.0f,
                1.0f, 1.5f, 0.0f,
                1.0f, -0.35f, 0};
        //coordinates for box
        float vertexArray4[] = {
                -0.5f, 0f, 0.4f,
                -0.5f, -0.6f, 0.4f,
                0.5f, 0f, 0.4f,
                0.5f, -0.6f, 0.4f};

        //создадим буфер для хранения координат вершин
        ByteBuffer vertex = ByteBuffer.allocateDirect(vertexArray.length * 4);
        vertex.order(ByteOrder.nativeOrder());
        vertexBuffer = vertex.asFloatBuffer();
        vertexBuffer.position(0);

        ByteBuffer vertex1 = ByteBuffer.allocateDirect(vertexArray1.length * 4);
        vertex1.order(ByteOrder.nativeOrder());
        vertexBuffer1 = vertex1.asFloatBuffer();
        vertexBuffer1.position(0);

        ByteBuffer vertex4 = ByteBuffer.allocateDirect(vertexArray4.length * 4);
        vertex4.order(ByteOrder.nativeOrder());
        vertexBuffer4 = vertex4.asFloatBuffer();
        vertexBuffer4.position(0);

        //перепишем координаты вершин из массива в буфер
        vertexBuffer.put(vertexArray);
        vertexBuffer.position(0);
        vertexBuffer1.put(vertexArray1);
        vertexBuffer1.position(0);
        vertexBuffer4.put(vertexArray4);
        vertexBuffer4.position(0);
        //вектор нормали перпендикулярен плоскости квадрата
        //и направлен вдоль оси Z
        float nx = 0;
        float ny = 0;
        float nz = 1;
        //нормаль одинакова для всех вершин квадрата,
        //поэтому переписываем координаты вектора нормали в массив 4 раза
        float normalArray[] = {nx, ny, nz, nx, ny, nz, nx, ny, nz, nx, ny, nz};
        //создадим буфер для хранения координат векторов нормали
        ByteBuffer normal = ByteBuffer.allocateDirect(normalArray.length * 4);
        normal.order(ByteOrder.nativeOrder());
        normalBuffer = normal.asFloatBuffer();
        normalBuffer.position(0);

        //перепишем координаты нормалей из массива в буфер
        normalBuffer.put(normalArray);
        normalBuffer.position(0);

        //цвет первой вершины - красный
        float red1 = 1;
        float green1 = 0;
        float blue1 = 0;
        //цвет второй вершины - зеленый
        float red2 = 0;
        float green2 = 1;
        float blue2 = 0;
        //цвет третьей вершины - синий
        float red3 = 0;
        float green3 = 0;
        float blue3 = 1;
        //цвет четвертой вершины - желтый
        float red4 = 1;
        float green4 = 1;
        float blue4 = 0;
        //перепишем цвета вершин в массив
        //четвертый компонент цвета (альфу) примем равным единице
        float colorArray[] = {
                red1, green1, blue1, 1,
                red2, green2, blue2, 1,
                red3, green3, blue3, 1,
                red4, green4, blue4, 1,
        };
        float colorArray1[] = {
                0.2f, 0.2f, 0.8f, 1,
                0.5f, 0.5f, 1, 1,
                0.2f, 0.2f, 0.8f, 1,
                0.5f, 0.5f, 1, 1,
        };
        float colorArray4[] = {
                1, 1, 1, 1,
                0.2f, 0.2f, 0.2f, 1,
                1, 1, 1, 1,
                0.2f, 0.2f, 0.2f, 1,
        };

        //создадим буфер для хранения цветов вершин
        ByteBuffer color = ByteBuffer.allocateDirect(colorArray.length * 4);
        color.order(ByteOrder.nativeOrder());
        colorBuffer = color.asFloatBuffer();
        colorBuffer.position(0);
        //перепишем цвета вершин из массива в буфер
        colorBuffer.put(colorArray);
        colorBuffer.position(0);

        ByteBuffer color1 = ByteBuffer.allocateDirect(colorArray1.length * 4);
        color1.order(ByteOrder.nativeOrder());
        colorBuffer1 = color1.asFloatBuffer();
        colorBuffer1.position(0);
        colorBuffer1.put(colorArray1);
        colorBuffer1.position(0);

        ByteBuffer color4 = ByteBuffer.allocateDirect(colorArray4.length * 4);
        color4.order(ByteOrder.nativeOrder());
        colorBuffer4 = color4.asFloatBuffer();
        colorBuffer4.position(0);
        colorBuffer4.put(colorArray4);
        colorBuffer4.position(0);

    }//конец конструктора

    //метод, который срабатывает при изменении размеров экрана
    //в нем мы получим матрицу проекции и матрицу модели-вида-проекции
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // устанавливаем glViewport
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float k = 0.055f;
        float left = -k * ratio;
        float right = k * ratio;
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
        String vertexShaderCode =
                "uniform mat4 u_modelViewProjectionMatrix;" +
                        "attribute vec3 a_vertex;" +
                        "attribute vec3 a_normal;" +
                        "attribute vec4 a_color;" +
                        "varying vec3 v_vertex;" +
                        "varying vec3 v_normal;" +
                        "varying vec4 v_color;" +
                        "void main() {" +
                        "v_vertex=a_vertex;" +
                        "vec3 n_normal=normalize(a_normal);" +
                        "v_normal=n_normal;" +
                        "v_color=a_color;" +
                        "gl_Position = u_modelViewProjectionMatrix * vec4(a_vertex,1.0);" +
                        "}";
        //записываем код фрагментного шейдера в виде строки
        String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec3 u_camera;" +
                        "uniform vec3 u_lightPosition;" +
                        "varying vec3 v_vertex;" +
                        "varying vec3 v_normal;" +
                        "varying vec4 v_color;" +
                        "void main() {" +
                        "vec3 n_normal=normalize(v_normal);" +
                        "vec3 lightvector = normalize(u_lightPosition - v_vertex);" +
                        "vec3 lookvector = normalize(u_camera - v_vertex);" +
                        "float ambient=0.2;" +
                        "float k_diffuse=0.3;" +
                        "float k_specular=0.5;" +
                        "float diffuse = k_diffuse * max(dot(n_normal, lightvector), 0.0);" +
                        "vec3 reflectvector = reflect(-lightvector, n_normal);" +
                        "float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );" +
                        "vec4 one=vec4(1.0,1.0,1.0,1.0);" +
                        "vec4 lightColor = (ambient+diffuse+specular)*one;" +
                        "gl_FragColor = mix(lightColor,v_color,0.6);" +
                        "}";

        String vertexShaderCode1 =
                "uniform mat4 u_modelViewProjectionMatrix;\n" +
                        "attribute vec3 a_vertex;\n" +
                        "attribute vec3 a_normal;\n" +
                        "attribute vec4 a_color;\n" +
                        "varying vec3 v_vertex;\n" +
                        "varying vec3 v_normal;\n" +
                        "varying vec4 v_color;\n" +
                        "// определяем переменные для передачи \n" +
                        "// координат двух текстур на интерполяцию\n" +
                        "varying vec2 v_texcoord0;\n" +
                        "varying vec2 v_texcoord1;\n" +
                        "void main() {\n" +
                        "        v_vertex=a_vertex;\n" +
                        "        vec3 n_normal=normalize(a_normal);\n" +
                        "        v_normal=n_normal;\n" +
                        "        v_color=a_color;\n" +
                        "        //вычисляем координаты первой текстуры и отравляем их на интерполяцию\n" +
                        "        //пусть координата текстуры S будет равна координате вершины X\n" +
                        "        v_texcoord0.s=a_vertex.x;\n" +
                        "        //а координата текстуры T будет равна координате вершины Z\n" +
                        "        v_texcoord0.t=a_vertex.z;\n" +
                        "        gl_Position = u_modelViewProjectionMatrix * vec4(a_vertex,1.0);" +
                        "}";
        //записываем код фрагментного шейдера в виде строки
        String fragmentShaderCode2 = "" +
                "precision mediump float;\n" +
                "uniform vec3 u_camera;\n" +
                "uniform vec3 u_lightPosition;\n" +
                "uniform sampler2D u_texture0;\n" +
                "uniform sampler2D u_texture1;\n" +
                "varying vec3 v_vertex;\n" +
                "varying vec3 v_normal;\n" +
                "varying vec4 v_color;\n" +
                "// принимаем координат двух текстур после интерполяции\n" +
                "varying vec2 v_texcoord0;\n" +
                "varying vec2 v_texcoord1;\n" +
                "void main() {\n" +
                "       vec3 n_normal=normalize(v_normal);\n" +
                "       vec3 lightvector = normalize(u_lightPosition - v_vertex);\n" +
                "       vec3 lookvector = normalize(u_camera - v_vertex);\n" +
                "       float ambient=0.2;\n" +
                "       float k_diffuse=0.8;\n" +
                "       float k_specular=0.4;\n" +
                "       float diffuse = k_diffuse * max(dot(n_normal, lightvector), 0.0);\n" +
                "       vec3 reflectvector = reflect(-lightvector, n_normal);\n" +
                "       float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );\n" +
                "      vec4 one=vec4(1.0,1.0,1.0,1.0);\n" +
                "      //оставим пока квадрат временно без освещения и выполним смешивание текстуры\n" +
                " //вычисляем координаты первой текстуры\n" +
                "      float r = v_vertex.x * v_vertex.x + v_vertex.z * v_vertex.z;\n" +
                "      vec2 texcoord0 = 0.3 * r * v_vertex.xz;\n" +
                "      //вычисляем цвет пикселя для первой текстуры\n" +
                "      vec4 textureColor0=texture2D(u_texture0, texcoord0);\n" +
                "\n" +
                "      //вычисляем координаты второй текстуры\n" +
                "      //пусть они будут пропорциональны координатам пикселя\n" +
                "      //подберем коэффициенты так, \n" +
                "      //чтобы вторая текстура заполнила весь квадрат\n" +
                "      vec2 texcoord1=0.25*(v_vertex.xz-2.0);\n" +
                "      //вычисляем цвет пикселя для второй текстуры\n" +
                "      vec4 textureColor1=texture2D(u_texture1, texcoord1);\n" +
                "\n" +
                "      //умножим цвета первой и второй текстур\n" +
                "      //gl_FragColor =textureColor0*textureColor1;\n" +
                "gl_FragColor=2.0*(ambient+diffuse)*mix(textureColor0,textureColor1,0.5)+specular*one;" +
                "}";

        //создадим шейдерный объект
        texture = new Texture(context, R.drawable.grass);

        mShader = new com.example.ghat_gpt_renderer.light_texture.Shader(vertexShaderCode1, fragmentShaderCode2);
        //свяжем буфер вершин с атрибутом a_vertex в вершинном шейдере
        mShader.linkVertexBuffer(vertexBuffer);
        //свяжем буфер нормалей с атрибутом a_normal в вершинном шейдере
        mShader.linkNormalBuffer(normalBuffer);
        //свяжем буфер цветов с атрибутом a_color в вершинном шейдере
        mShader.linkColorBuffer(colorBuffer);
        mShader.linkTexture(texture, null);
        //связь атрибутов с буферами сохраняется до тех пор,
        //пока не будет уничтожен шейдерный объект

        mShader1 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader1.linkVertexBuffer(vertexBuffer1);
        mShader1.linkNormalBuffer(normalBuffer);
        mShader1.linkColorBuffer(colorBuffer1);


    }

    //метод, в котором выполняется рисование кадра
    public void onDrawFrame(GL10 unused) {
        drawFrame();
    }

    private void drawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //передаем в шейдерный объект матрицу модели-вида-проекции
        mShader.linkVertexBuffer(vertexBuffer);
       // mShader.linkColorBuffer(colorBuffer);
        mShader.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader.linkCamera(xCamera, yCamera, zCamera);
        mShader.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        mShader.linkTexture(texture, null);
        mShader.useProgram();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mShader1.linkVertexBuffer(vertexBuffer1);
        mShader1.linkColorBuffer(colorBuffer1);
        mShader1.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader1.linkCamera(xCamera, yCamera, zCamera);
        mShader1.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        mShader1.useProgram();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


    }

}