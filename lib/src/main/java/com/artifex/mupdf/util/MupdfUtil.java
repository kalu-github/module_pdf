package com.artifex.mupdf.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.artifex.mupdf.fitz.Document;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class MupdfUtil {

    public static final Document openFromRaw(final Context context, final int id) {

        try {

            // step1
            final InputStream stm = context.getResources().openRawResource(id);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();

            // step2
            final byte[] buf = new byte[16384];
            int i;
            while ((i = stm.read(buf)) != -1) {
                out.write(buf, 0, i);
            }

            // step3
            out.flush();
            final byte[] bytes = out.toByteArray();

            // step4
            if (null != stm) {
                stm.close();
            }

            // step5
            if (null != out) {
                out.close();
            }

            // step6
            return Document.openDocument(bytes, "application/pdf");
        } catch (Exception e) {
            Toast.makeText(context, "打开文件失败", Toast.LENGTH_SHORT).show();
            Log.e("kalu", "MupdfUtil ==> " + e.getMessage(), e);
            return null;
        }
    }

    public static final Document openFromInputStream(final Context context, final InputStream stm) {

        try {

            // step1
            final ByteArrayOutputStream out = new ByteArrayOutputStream();

            // step2
            final byte[] buf = new byte[16384];
            int i;
            while ((i = stm.read(buf)) != -1) {
                out.write(buf, 0, i);
            }

            // step3
            out.flush();
            final byte[] bytes = out.toByteArray();

            // step4
            if (null != stm) {
                stm.close();
            }

            // step5
            if (null != out) {
                out.close();
            }

            // step6
            return Document.openDocument(bytes, "application/pdf");
        } catch (Exception e) {
            Toast.makeText(context, "打开文件失败", Toast.LENGTH_SHORT).show();
            Log.e("kalu", "MupdfUtil ==> " + e.getMessage(), e);
            return null;
        }
    }

    public static final Document openFromByte(final Context context, final byte[] bytes) {

        try {
            return Document.openDocument(bytes, "application/pdf");
        } catch (Exception e) {
            Toast.makeText(context, "打开文件失败", Toast.LENGTH_SHORT).show();
            Log.e("kalu", "MupdfUtil ==> " + e.getMessage(), e);
            return null;
        }
    }

    public static final Document openFromAsset(final Context context, final String name) {

        try {

            // step1
            final InputStream stm = context.getAssets().open(name);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();

            // step2
            final byte[] buf = new byte[16384];
            int i;
            while ((i = stm.read(buf)) != -1) {
                out.write(buf, 0, i);
            }

            // step3
            out.flush();
            final byte[] bytes = out.toByteArray();

            // step4
            if (null != stm) {
                stm.close();
            }

            // step5
            if (null != out) {
                out.close();
            }

            // step6
            return Document.openDocument(bytes, "application/pdf");
        } catch (Exception e) {
            Toast.makeText(context, "打开文件失败", Toast.LENGTH_SHORT).show();
            Log.e("kalu", "MupdfUtil ==> " + e.getMessage(), e);
            return null;
        }
    }

    public static final Document openFromLocal(final Context context, final String name) {

        try {
            return Document.openDocument(name);
        } catch (Exception e) {
            Toast.makeText(context, "打开文件失败", Toast.LENGTH_SHORT).show();
            Log.e("kalu", "MupdfUtil ==> " + e.getMessage(), e);
            return null;
        }
    }
}
