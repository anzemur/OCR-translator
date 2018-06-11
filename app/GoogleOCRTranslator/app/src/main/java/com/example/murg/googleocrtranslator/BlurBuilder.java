package com.example.murg.googleocrtranslator;

/**
 * Created by likewise on 28.3.2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.RequiresApi;
import android.view.View;


public class BlurBuilder {
    public static final float BITMAP_SCALE_OFF = 0.4f;
    public static final float BITMAP_SCALE_ON = 0.07f;

    public static final float BLUR_RADIUS = 24.5f;
    private static float blurScale = BITMAP_SCALE_OFF;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap blur(View v) {
        return blur(v.getContext(), getScreenshot(v));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap blur(Context ctx, Bitmap image) {
        int width = Math.round(image.getWidth() * blurScale);
        int height = Math.round(image.getHeight() * blurScale);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(ctx);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public static void setBlurScale(float scale) {
        blurScale = scale;
    }

    private static Bitmap getScreenshot(View v) {

        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
}