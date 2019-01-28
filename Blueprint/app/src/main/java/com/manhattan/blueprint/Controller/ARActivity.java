package com.manhattan.blueprint.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ARActivity extends AppCompatActivity {
    private ArFragment arFragment;

    private boolean userRequestedARInstall = false;
    private ModelRenderable manhattanRenderable;
    private ViewRenderable testViewRenderable;
    private Resource resourceToCollect;

    private final int tapsRequired = 5;
    private int collectCounter;
    private boolean itemWasPlaced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        String jsonResource = (String) getIntent().getExtras().get("resource");
        Gson gson = new GsonBuilder().create();
        resourceToCollect = gson.fromJson(jsonResource, Resource.class);

        PermissionManager cameraPermissionManager = new PermissionManager(0, Manifest.permission.CAMERA);
        if (!cameraPermissionManager.hasPermission(this)) {
            createDialog(getString(R.string.permission_camera_title),
                    getString(R.string.permission_camera_description),
                    (dialog, which) -> finish());
        }
        itemWasPlaced = false;
        collectCounter = tapsRequired - 1;
    }

    public void onUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arFragment != null) return;
        // Ensure latest version of ARCore is installed
        try {
            switch (ArCoreApk.getInstance().requestInstall(this, !userRequestedARInstall)) {
                case INSTALLED:
                    startAr();

                case INSTALL_REQUESTED:
                    // Ensures next call of request install returns INSTALLED or throws
                    userRequestedARInstall = true;
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            createDialog(getString(R.string.ar_install_title),
                    getString(R.string.ar_install_description),
                    (dialog, which) -> finish());
        } catch (Exception e) {
            createDialog(getString(R.string.whoops_title),
                    getString(R.string.whoops_description) + e.toString(),
                    (dialog, which) -> finish());
        }
    }

    private void startAr() {
        // Build renderable object
        ViewRenderable.builder()
                .setView(this, R.layout.resource_ar)
                .build()
                .thenAccept(renderable -> {
                    testViewRenderable = renderable;
                    TextView resourceView = renderable.getView().findViewById(R.id.Resource_AR);
                    resourceView.setText(resourceToCollect.getId());
                });

        // Start AR:
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById((R.id.ux_fragment));
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);

        // TODO: Uncomment to remove icon of a hand with device
        // arFragment.getPlaneDiscoveryController().hide();
        // arFragment.getPlaneDiscoveryController().setInstructionView(null);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (testViewRenderable == null) {
                        return;
                    } else if (!itemWasPlaced) {
                        // Create the Anchor.
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        // Create the transformable node and add it to the anchor.
                        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                        node.setParent(anchorNode);
                        node.setRenderable(testViewRenderable);
                        node.select();

                        node.setOnTapListener((hitTestResult, motionEvent1) -> {
                            onResourceTapped();
                        });
                        node.getTranslationController().setEnabled(false);

                        // Remove plane renderer
                        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                        itemWasPlaced = true;
                    }
                });
    }

    private void onResourceTapped() {
        if (collectCounter > 0) {
            int progress = ((tapsRequired - collectCounter) * 100) / tapsRequired;
            String progress_msg = String.format(getString(R.string.collection_progress), progress);
            Toast.makeText(this, progress_msg, Toast.LENGTH_SHORT).show();
            collectCounter--;
        } else if (collectCounter == 0) {
            InventoryItem itemCollected = new InventoryItem(resourceToCollect.getId(), resourceToCollect.getQuantity());
            BlueprintAPI api = new BlueprintAPI(this);
            Inventory inventoryToAdd = new Inventory(new ArrayList<>(Collections.singletonList(itemCollected)));
            api.makeRequest(api.inventoryService.addToInventory(inventoryToAdd), new APICallback<Void>() {
                @Override
                public void success(Void response) {
                    // Show success with "You collected 5 wood", defaulting to "You collected 5 items"
                    String itemName = ItemManager.getInstance(ARActivity.this).getName(resourceToCollect.getId()).getWithDefault("items");
                    String successMsg = String.format(getString(R.string.collection_success), resourceToCollect.getQuantity(), itemName);
                    Toast.makeText(ARActivity.this, successMsg, Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void failure(int code, String error) {
                    createDialog(getString(R.string.collection_failure_title), error, (dialog, which) -> dialog.dismiss());
                }
            });
        }
    }

    private void createDialog(String title, String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ARActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            onClick.onClick(dialog, which);
        });
        alertDialog.create().show();
    }
}
