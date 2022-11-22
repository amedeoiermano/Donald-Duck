package com.xayris.donalduck.utils;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;

public class Utility {

    static Toast _globalToast;

    public static void showToast(Context context, int messageResource, int duration)
    {
        if(_globalToast != null)
            _globalToast.cancel();
        _globalToast = Toast.makeText(context, messageResource, duration);
        _globalToast.show();
    }

    public static void hideToast()
    {
        if(_globalToast != null)
            _globalToast.cancel();
    }

    public static DisplayMetrics getDisplayMetrics(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    public static void showKeyboard(View view, Activity context) {
        view.requestFocus();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
        },200);
    }
    public static void hideKeyboard(Activity context) {
        new Handler(Looper.getMainLooper()).post(() -> {
            View view = context.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    private static final long SHORT_HAPTIC_FEEDBACK_DURATION = 50L;

    public static void performHapticFeedback(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(SHORT_HAPTIC_FEEDBACK_DURATION, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(vibrationEffect);
        } else {
            vibrator.vibrate(TimeUnit.MILLISECONDS.toMillis(SHORT_HAPTIC_FEEDBACK_DURATION));
        }
    }

    public static boolean isLandscape(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

}
