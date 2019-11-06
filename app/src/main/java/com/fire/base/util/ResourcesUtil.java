package com.fire.base.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class ResourcesUtil {

    private static Context sContext;
    private static Resources sRes;

    public static void init(Context context) {
        sContext = context;
        sRes = context.getResources();
    }

    public static String getString(String id) {
        int resId = sRes.getIdentifier(id, "string", sContext.getPackageName());
        if (resId == 0) {
            return id;
        }
        return sRes.getString(resId);
    }

    public static String getString(int id) {
        if (sContext != null) {
            return sContext.getString(id);
        }
        return "";
    }

    public static CharSequence getText(int id) {
        return sContext.getText(id);
    }

    public static int getDrawable(String id) {
        return sRes.getIdentifier(id, "drawable", sContext.getPackageName());
    }

    public static Drawable getDrawable(int id) {
        return ContextCompat.getDrawable(sContext, id);
    }

    public static int getColor(int id) {
        if (Build.VERSION.SDK_INT >= 23) {
            return sRes.getColor(id, sContext.getTheme());
        }

        return sRes.getColor(id);
    }
}
