package com.manhattan.blueprint.Controller;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.widget.LinearLayout;

import com.manhattan.blueprint.R;

import jmini3d.android.Activity3d;
import jmini3d.android.input.InputController;

public class TestActivity extends Activity3d {
    InputController inputController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DemoScreenController screenController = new DemoScreenController();
        glSurfaceView3d.setScreenController(screenController);
        glSurfaceView3d.setLogFps(true);
        inputController = new InputController(glSurfaceView3d);
    }

    @Override
    protected void onCreateSetContentView() {
        super.onCreateSetContentView();
        setContentView(R.layout.activity_test);
        ((LinearLayoutCompat) findViewById(R.id.root)).addView(glSurfaceView3d);

    }
}
