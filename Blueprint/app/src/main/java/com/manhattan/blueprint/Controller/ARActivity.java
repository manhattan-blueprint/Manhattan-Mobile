package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.manhattan.blueprint.Model.PermissionManager;
import com.manhattan.blueprint.R;

public class ARActivity extends AppCompatActivity {
    private ArFragment arFragment;

    private boolean userRequestedARInstall = false;
    private ModelRenderable manhattanRenderable;
    private ViewRenderable testViewRenderable;

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
        if (arFragment != null) return;
        // Ensure latest version of ARCore is installed
        try {
            switch (ArCoreApk.getInstance().requestInstall(this, !userRequestedARInstall)) {
                case INSTALLED:
                    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById((R.id.ux_fragment));

                    // When you build a Renderable, Sceneform loads its resources in the background while returning
                    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
                    ViewRenderable.builder()
                            .setView(this, R.layout.planets)
                            .build()
                            .thenAccept(renderable -> testViewRenderable = renderable);

                    arFragment.setOnTapArPlaneListener(
                            (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                                if (testViewRenderable == null) {
                                    Log.d("NULL", "Renderable is null");
                                    return;
                                }

                                Log.d("NULL", "Renderable is not null");
                                // Create the Anchor.
                                Anchor anchor = hitResult.createAnchor();
                                AnchorNode anchorNode = new AnchorNode(anchor);
                                anchorNode.setParent(arFragment.getArSceneView().getScene());

                                // Create the transformable andy and add it to the anchor.
                                TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                                andy.setParent(anchorNode);
                                andy.setRenderable(testViewRenderable);
                                andy.select();
                            });

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