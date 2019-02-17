package com.manhattan.blueprint.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ViewUtils {
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
}

