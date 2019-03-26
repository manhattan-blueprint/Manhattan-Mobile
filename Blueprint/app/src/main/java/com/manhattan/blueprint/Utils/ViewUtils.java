package com.manhattan.blueprint.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;

import com.manhattan.blueprint.Controller.ARActivity;
import com.manhattan.blueprint.R;

public class ViewUtils {
    public static void createDialog(Context context, String title, String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(R.string.positive_response, (dialog, which) -> {
            dialog.dismiss();
            onClick.onClick(dialog, which);
        });
        alertDialog.create().show();
    }

    public static void showError(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog
                .Builder(context, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.ok, listener)
                .show();
    }

    public static void showError(Context context, String title, String message) {
        showError(context, title, message, null);
    }

    public static float dpToPx(Context context, float dp){
        return dp * (context.getResources().getDisplayMetrics().densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }
}

