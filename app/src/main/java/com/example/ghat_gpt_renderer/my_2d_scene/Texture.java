package com.example.ghat_gpt_renderer.my_2d_scene;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Texture {
    //создаем поле для хранения имени текстуры
    private int name;
    // конструктор двумерной текстуры из ресурса
    //передаем в качестве аргументов контекст
    //и идентификатор ресурса графического файла
    public Texture(Context context, int idpicture) {
        //создаем пустой массив из одного элемента
        //в этот массив OpenGL ES запишет свободный номер текстуры,
        // который называют именем текстуры
        int []names = new int[1];
        // получаем свободное имя текстуры, которое будет записано в names[0]
        GLES20.glGenTextures(1, names, 0);
        //запомним имя текстуры в локальном поле класса
        name = names[0];
        //теперь мы можем обращаться к текстуре по ее имени name
        //устанавливаем режим выравнивания по байту
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        //делаем текстуру с именем name текущей
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, name);
        //устанавливаем фильтры текстуры
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        //устанавливаем режим повтора изображения
        //если координаты текстуры вышли за пределы от 0 до 1
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);
        // загружаем картинку в Bitmap из ресурса
        Bitmap bitmap =
                BitmapFactory.decodeResource(context.getResources(), idpicture);
        //переписываем Bitmap в память видеокарты
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // удаляем Bitmap из памяти, т.к. картинка уже переписана в видеопамять
        bitmap.recycle();
        // Важный момент !
        // Создавать мипмапы нужно только
        // после загрузки текстуры в видеопамять
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }// конец конструктора двумерной текстуры

    //нам будет нужен метод, который возвращает имя текстуры
    public int getName() {
        return name;
    }
}