package com.manhattan.blueprint.Controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
            EditText hololensIP = findViewById(R.id.settings_hololens_ip);
            hololensIP.setText(session.hololensIP);

            findViewById(R.id.settings_save).setOnClickListener(v -> {
                dao.setSession(new Session(session.getUsername(), hololensIP.getText().toString()));
                this.runOnUiThread(this::finish);
            });
        });
    }


}
