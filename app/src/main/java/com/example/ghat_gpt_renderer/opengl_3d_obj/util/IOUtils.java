package com.example.ghat_gpt_renderer.opengl_3d_obj.util;

import android.os.ParcelFileDescriptor;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    private IOUtils() {
    }


    public static void closeQuietly(@Nullable Closeable var0) {
        if (var0 != null) {
            try {
                var0.close();
                return;
            } catch (IOException var1) {
            }
        }

    }


    public static void closeQuietly(@Nullable ParcelFileDescriptor var0) {
        if (var0 != null) {
            try {
                var0.close();
                return;
            } catch (IOException var1) {
            }
        }

    }


    public static boolean isGzipByteBuffer(byte[] var0) {
        return var0.length > 1 && (var0[0] & 255 | (var0[1] & 255) << 8) == 35615;
    }


    public static long copyStream(InputStream var0, OutputStream var1) throws IOException {
        return zza(var0, var1, false);
    }

    private static long zza(InputStream var0, OutputStream var1, boolean var2) throws IOException {
        return copyStream(var0, var1, var2, 1024);
    }


    public static long copyStream(InputStream var0, OutputStream var1, boolean var2, int var3) throws IOException {
        byte[] var4 = new byte[var3];
        long var5 = 0L;

        int var7;
        try {
            while ((var7 = var0.read(var4, 0, var3)) != -1) {
                var5 += (long) var7;
                var1.write(var4, 0, var7);
            }
        } finally {
            if (var2) {
                closeQuietly((Closeable) var0);
                closeQuietly((Closeable) var1);
            }

        }

        return var5;
    }


    public static byte[] readInputStreamFully(InputStream var0) throws IOException {
        return readInputStreamFully(var0, true);
    }


    public static byte[] readInputStreamFully(InputStream var0, boolean var1) throws IOException {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        zza(var0, var2, var1);
        return var2.toByteArray();
    }


    public static byte[] toByteArray(InputStream var0) throws IOException {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        ByteArrayOutputStream var3 = var1;
        InputStream var2 = var0;
        byte[] var4 = new byte[4096];

        int var5;
        while ((var5 = var2.read(var4)) != -1) {
            var3.write(var4, 0, var5);
        }

        return var1.toByteArray();
    }
}
