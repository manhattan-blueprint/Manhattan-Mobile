package com.manhattan.blueprint.Model.Managers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionManager {
    private int permissionCode;
    private String permission;

    public PermissionManager(int permissionCode, String permission) {
        this.permissionCode = permissionCode;
        this.permission = permission;
    }

    /**
     * Check to see we have the necessary permissions for this app.
     */
    public boolean hasPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    public void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCode);
    }

    /**
     * Check to see if we need to show the rationale for this permission.
     */
    public boolean shouldShowRequestPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Launch Application Setting to grant permission.
     */
    public void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
