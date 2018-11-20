package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.manhattan.blueprint.Model.PermissionManager;
import com.manhattan.blueprint.R;

public class ARActivity extends AppCompatActivity {
    private Session session;
    private boolean userRequestedARInstall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        PermissionManager cameraPermissionManager = new PermissionManager(0, Manifest.permission.CAMERA);
        if (!cameraPermissionManager.hasPermission(this)) {
            createDialog("Camera required",
                    "Please grant access to your camera so Blueprint can show resources around you.",
                    (dialog, which) -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null) return;
        // Ensure latest version of ARCore is installed
        try {
            switch (ArCoreApk.getInstance().requestInstall(this, !userRequestedARInstall)) {
                case INSTALLED:
                    session = new Session(this);
                case INSTALL_REQUESTED:
                    // Ensures next call of request install returns INSTALLED or throws
                    userRequestedARInstall = true;
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            createDialog("ARCore required!",
                    "We require ARCore to show you resources. Please install and try again",
                    (dialog, which) -> finish());
        } catch (Exception e) {
            createDialog("Whoops!",
                    "Something went wrong: " + e.toString(),
                    (dialog, which) -> finish());
        }
    }

    private void createDialog(String title, String message, DialogInterface.OnClickListener onClick){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ARActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
            onClick.onClick(dialog, which);
        });
        alertDialog.create().show();
    }
}
