package com.manhattan.blueprint.Controller;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BlueprintDAO dao = BlueprintDAO.getInstance(this);

        dao.getSession().ifPresent(session -> {
            Button toggleHololens = findViewById(R.id.toggle_hololens);
            EditText hololensIP = findViewById(R.id.settings_hololens_ip);
            hololensIP.setText(session.hololensIP);

            toggleHololens.setOnClickListener(v -> {
                dao.setSession(new Session(session.getUsername(),
                        session.getAccountType(),
                        hololensIP.getText().toString(),
                        !session.isHololensConnected()));
                toggleHololens.setTextColor(Color.argb(255,0,0,255));
                this.runOnUiThread(this::finish);
            });

            findViewById(R.id.settings_save).setOnClickListener(v -> {
                dao.setSession(new Session(session.getUsername(),
                                           session.getAccountType(),
                                           hololensIP.getText().toString(),
                                           session.isHololensConnected()));
                this.runOnUiThread(this::finish);
            });
        });
    }


}
