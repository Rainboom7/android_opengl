package com.example.ghat_gpt_renderer.my_2d_scene;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.ghat_gpt_renderer.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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
    private Shader mShader;
    private Texture grassTexture, boxTexture;

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
        float x1=-1;
        float y1=-1;
        float z1=0;
        //координаты вершины 2
        float x2=-1;
        float y2=-1;
        float z2=1;
        //координаты вершины 3
        float x3=1;
        float y3=1;
        float z3=0;
        //координаты вершины 4
        float x4=1;
        float y4=1;
        float z4=1;
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
                "        gl_Position = u_modelViewProjectionMatrix * vec4(a_vertex,1.0);"+
                "}";
        //записываем код фрагментного шейдера в виде строки
        String fragmentShaderCode=
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
                        "gl_FragColor=2.0*(ambient+diffuse)*mix(textureColor0,textureColor1,0.5)+specular*one;"+
                        "}";


        //создаем текстурные объекты из картинок
        grassTexture =new Texture(context, R.drawable.grass);
        boxTexture =new Texture(context,R.drawable.box2);

        //создадим шейдерный объект
        mShader=new Shader(vertexShaderCode, fragmentShaderCode);
        //свяжем буфер вершин с атрибутом a_vertex в вершинном шейдере
        mShader.linkVertexBuffer(vertexBuffer);
        //свяжем буфер нормалей с атрибутом a_normal в вершинном шейдере
        mShader.linkNormalBuffer(normalBuffer);
        //свяжем буфер цветов с атрибутом a_color в вершинном шейдере
        mShader.linkColorBuffer(colorBuffer);
        // свяжем текстурные объекты с сэмплерами в фрагментном шейдере
        mShader.linkTexture(null, boxTexture);

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