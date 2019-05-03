package com.manhattan.blueprint.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.manhattan.blueprint.R;

import java.util.ArrayList;
import java.util.List;

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

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static ArrayList<View> getChildren(View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<>();
        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(getChildren(child));
            result.addAll(viewArrayList);
        }
        return result;
    }

    public static <T> ArrayAdapter<T> makeSpinner(Activity activity, List<T> l) {
        return new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, l);
    }
}

