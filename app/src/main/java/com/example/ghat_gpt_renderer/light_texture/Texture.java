package com.example.ghat_gpt_renderer.light_texture;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;

import java.nio.FloatBuffer;
import android.opengl.GLES20;
import android.content.Context;
import android.opengl.GLUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
       // GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        //делаем текстуру с именем name текущей


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap bitmap =
                BitmapFactory.decodeResource(context.getResources(), idpicture,options);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, name);
        //устанавливаем фильтр        glActiveTexture(GL_TEXTURE0);ы текстуры
        glActiveTexture(GL_TEXTURE0);


        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);



        //переписываем Bitmap в память видеокарты
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // удаляем Bitmap из памяти, т.к. картинка уже переписана в видеопамять
        bitmap.recycle();
        glBindTexture(GL_TEXTURE_2D, 0);

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