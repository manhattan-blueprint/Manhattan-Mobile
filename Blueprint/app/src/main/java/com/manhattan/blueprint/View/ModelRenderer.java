package com.manhattan.blueprint.View;

import android.content.Context;
import android.view.MotionEvent;
import android.view.Surface;

import com.manhattan.blueprint.Model.DAO.Maybe;
import com.manhattan.blueprint.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.view.SurfaceView;

public class ModelRenderer extends Renderer {
    private Context context;
    private float rotationSpeed;
    private int backgroundColor;
    private int modelID;
    private Object3D model;

    public ModelRenderer(Context context, int modelID, int backgroundColor, float rotationSpeed) {
        super(context);
        this.context = context;
        this.modelID = modelID;
        this.backgroundColor = backgroundColor;
        this.rotationSpeed = rotationSpeed;
        setFrameRate(60);
    }

    public void initScene() {
        DirectionalLight directionalLightLeft = new DirectionalLight(-1f, .2f, -1.0f);
        directionalLightLeft.setColor(1.0f, 1.0f, 1.0f);
        directionalLightLeft.setPower(1);
        getCurrentScene().addLight(directionalLightLeft);

        DirectionalLight directionalLightRight = new DirectionalLight(1f, .2f, -1.0f);
        directionalLightRight.setColor(1.0f, 1.0f, 1.0f);
        directionalLightRight.setPower(1);
        getCurrentScene().addLight(directionalLightRight);

        Maybe<Integer> modelImageID = getResourceID(context, "model" + modelID + "_obj");
        if (!modelImageID.isPresent()) {
            modelImageID = getResourceID(context, "modeldefault_obj");
        }

        try {
            LoaderOBJ loaderOBJ = new LoaderOBJ(context.getResources(), getTextureManager(), modelImageID.get());
            loaderOBJ.parse();
            model = loaderOBJ.getParsedObject();

            getCurrentScene().addChild(model);
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        getCurrentCamera().setY(getCurrentCamera().getY() + 0.5f);
        getCurrentScene().setBackgroundColor(backgroundColor);
    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        model.rotate(Vector3.Axis.Y, rotationSpeed);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    private static Maybe<Integer> getResourceID(Context context, String resName) {
        try {
            int identifier = context.getResources().getIdentifier(resName, "raw", context.getPackageName());
            return identifier == 0 ? Maybe.empty() : Maybe.of(identifier);
        } catch (Exception e) {
            return Maybe.empty();
        }
    }
}
