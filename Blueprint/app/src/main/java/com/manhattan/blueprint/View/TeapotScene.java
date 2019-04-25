package com.manhattan.blueprint.View;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Vector3;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.SkyboxGeometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;
import jmini3d.material.Material;
import jmini3d.material.PhongMaterial;

public class TeapotScene extends Scene {

    public TeapotScene() {

        Color4 transparent = new Color4(0, 0, 0, 0);
        Color4 white = new Color4(255, 255, 255, 255);

        PhongMaterial mirrorMat = new PhongMaterial(white, white, white);

        VariableGeometry skyboxGeometry = new SkyboxGeometry(300);
        Material skyboxMaterial = new Material();
        Object3d skybox = new Object3d(skyboxGeometry, skyboxMaterial);
        addChild(skybox);

        Geometry teapotGeometry = new DiamondGeometry();
        Object3d teapotO3d = new Object3d(teapotGeometry, mirrorMat);
        teapotO3d.setPosition(0, 0, -0.5f);
        addChild(teapotO3d);

        addLight(new AmbientLight(new Color4(255, 255, 255), 0.1f));
        addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255)));
        addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0), 1));
        addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0, 255, 0)));
    }
}
