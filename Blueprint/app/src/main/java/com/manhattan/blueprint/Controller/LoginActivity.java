package com.manhattan.blueprint.Controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Button;
import android.widget.*;
import android.util.Log;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.UserCredentials;
import com.manhattan.blueprint.R;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button   loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    public void onLoginClick(View view) {
        loginButton   = findViewById(R.id.loginButton);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        String usernameText = usernameInput.getText().toString();
        String passwordText = passwordInput.getText().toString();

        // check for empty / null username or password
        if( usernameText.equals(null) || usernameText.equals("") ) {
            usernameInput.setError("Empty username");
        }
        else if( passwordText.equals(null) || passwordText.equals("") ) {
            passwordInput.setError("Empty password");
        }
        else {
            loginButton.setEnabled(false);
            UserCredentials credentials = new UserCredentials(usernameText, passwordText);

            BlueprintAPI api = new BlueprintAPI();
            api.authenticate(credentials, new APICallback<Boolean>() {
                @Override
                public void success(Boolean response) {
                    // TODO: Add main game menu
                    // setContentView(R.layout.game_menu);
                    Log.d("loginMsg","Login Success!");
                }

                @Override
                public void failure(String error) {
                    loginButton.setEnabled(true);
                    Log.d("loginMsg","Error: " + error);
                }
            });
        }
    }
}
