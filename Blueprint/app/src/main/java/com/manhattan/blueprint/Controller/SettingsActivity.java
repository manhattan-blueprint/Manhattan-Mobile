package com.manhattan.blueprint.Controller;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.DAO.Consumer;
import com.manhattan.blueprint.Model.Session;
import com.manhattan.blueprint.R;

public class SettingsActivity extends AppCompatActivity {

    private int toggleOnColor  = Color.argb(255,0,180,0);
    private int toggleOffColor = Color.argb(255,235,0,0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button toggleHololens = findViewById(R.id.toggle_hololens);
        EditText hololensIP = findViewById(R.id.settings_hololens_ip);

        BlueprintDAO dao = BlueprintDAO.getInstance(this);

        dao.getSession().ifPresent(session -> {
                    hololensIP.setText(session.hololensIP);
                    if (session.isHololensConnected()) {
                        toggleHololens.setTextColor(toggleOnColor);
                    } else {
                        toggleHololens.setTextColor(toggleOffColor);
                    }
                });

            toggleHololens.setOnClickListener(v -> {
                dao.getSession().ifPresent(session -> {
                    dao.setSession(new Session(session.getUsername(),
                            session.getAccountType(),
                            hololensIP.getText().toString(),
                            !session.isHololensConnected()));
                int currentColor = toggleHololens.getCurrentTextColor();
                if (currentColor == toggleOnColor) {
                    toggleHololens.setTextColor(toggleOffColor);
                } else if (currentColor == toggleOffColor) {
                    toggleHololens.setTextColor(toggleOnColor);
                }
                });
            });

            findViewById(R.id.settings_save).setOnClickListener(v -> {
                dao.getSession().ifPresent(session -> {
                dao.setSession(new Session(session.getUsername(),
                                           session.getAccountType(),
                                           hololensIP.getText().toString(),
                                           session.isHololensConnected()));
                this.runOnUiThread(this::finish);
            });
        });
    }


}
