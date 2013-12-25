package com.polysfactory.scouter.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.polysfactory.scouter.C;

public class IOUtils {
    public static File getFilePath(Context context, String dirname, String filename) {
        File cascadeDir = context.getDir(dirname, Context.MODE_PRIVATE);
        return new File(cascadeDir, filename);
    }

    public static boolean copy(Context context, int res, File file) {
        InputStream is = context.getResources().openRawResource(res);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            Log.e(C.TAG, "file copy error", e);
            return false;
        } catch (IOException e) {
            Log.e(C.TAG, "file copy error", e);
            return false;
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static void takeScreenshot(View view, String filePath) {
        // create bitmap screen capture
        Bitmap bitmap;
        View v1 = view.getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        File imageFile = new File(filePath);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();

        } catch (FileNotFoundException e) {
            Log.e(C.TAG, "screenshot error", e);
        } catch (IOException e) {
            Log.e(C.TAG, "screenshot error", e);
        }
    }
}
