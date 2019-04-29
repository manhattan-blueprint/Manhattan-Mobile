package com.manhattan.blueprint.Controller;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.DAO.Consumer;
import com.manhattan.blueprint.Model.Session;
import com.manhattan.blueprint.R;

public class SettingsActivity extends AppCompatActivity {
    private Switch toggleHololens;
    private Button logout;
    private EditText hololensIP;
    private TextView usernameText;
    private TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.toggleHololens = findViewById(R.id.settings_hololens_switch);
        this.logout = findViewById(R.id.logout);
        this.hololensIP = findViewById(R.id.settings_hololens_ip);
        this.versionText = findViewById(R.id.settings_version);
        this.usernameText = findViewById(R.id.settings_username_text);

        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            hololensIP.setText(session.hololensIP);
            usernameText.setText(session.getUsername());
            toggleHololens.setChecked(session.isHololensConnected());
        });

        toggleHololens.setOnCheckedChangeListener(this::onToggleChangeListener);
        logout.setOnClickListener(this::onLogoutClickListener);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            this.versionText.setText("Version " + version);
        } catch (PackageManager.NameNotFoundException e) {
            this.versionText.setText("Unknown Version");
            e.printStackTrace();
        }
    }

    private void onToggleChangeListener(CompoundButton compoundButton, boolean b) {
        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            dao.setSession(new Session(session.getUsername(),
                    session.getAccountType(),
                    hololensIP.getText().toString(),
                    b));
            toggleHololens.setChecked(dao.getSession().get().isHololensConnected());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            dao.setSession(new Session(session.getUsername(),
                    session.getAccountType(),
                    hololensIP.getText().toString(),
                    session.isHololensConnected()));
            this.runOnUiThread(this::finish);
        });
    }

    private void onLogoutClickListener(View view) {
        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.clearSession();
        dao.clearTokens();

        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
