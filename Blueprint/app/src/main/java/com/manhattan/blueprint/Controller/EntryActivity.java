package com.manhattan.blueprint.Controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.manhattan.blueprint.Model.Managers.LoginManager;
import com.manhattan.blueprint.R;

public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginManager loginManager = new LoginManager(this);
        if (!loginManager.isLoggedIn()) {
            toOnboarding();
        } else {
            toMap();
        }
    }

    private void toOnboarding() {
        startActivity(new Intent(EntryActivity.this, OnboardingActivity.class));
        finish();
    }
    private void toMap() {
        startActivity(new Intent(EntryActivity.this, MapViewActivity.class));
        finish();
    }
}
