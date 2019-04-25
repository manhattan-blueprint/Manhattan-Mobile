package com.manhattan.blueprint.Controller;

import com.manhattan.blueprint.View.TeapotScene;

import jmini3d.Renderer3d;
import jmini3d.Scene;
import jmini3d.ScreenController;
import jmini3d.Vector3;

public class DemoScreenController implements ScreenController {
    float cameraAngle;
    long initialTime;

    int sceneIndex = 0;
    Scene scene = new TeapotScene();

    public DemoScreenController() {
        initialTime = System.currentTimeMillis();
    }

    @Override
    public boolean onNewFrame(boolean forceRedraw) {
        // Rotate camera...
        cameraAngle = 0.0005f * (System.currentTimeMillis() - initialTime);

        float d = 5;
        Vector3 target = scene.getCamera().getTarget();

//        scene.getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
//                (float) (target.y - d * Math.sin(cameraAngle)), //
//                target.z + (float) (d * Math.sin(cameraAngle)));

        scene.getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
                (float) (target.y - d * Math.sin(cameraAngle)), d / 2);
//
//        scene.getCamera().setPosition(target.x - d, target.y, target.z + d / 4);
        return true; // Render all the frames
    }

    @Override
    public void render(Renderer3d renderer3d) {
        renderer3d.render(scene);
    }
}
