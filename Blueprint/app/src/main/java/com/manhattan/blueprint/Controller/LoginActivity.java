package com.manhattan.blueprint.Controller;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Button;
import android.widget.*;
import android.content.DialogInterface;
import android.util.Log;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.UserCredentials;
import com.manhattan.blueprint.R;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput;
    EditText passwordInput;
    Button   loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton   = findViewById(R.id.loginButton);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
    }

    protected void onLoginClick(View view) {
        String usernameText = usernameInput.getText().toString();
        String passwordText = passwordInput.getText().toString();

        // check for empty / null username or password
        if(        usernameText == null || usernameText.equals("") ) {
            usernameInput.setError("Empty username");
        } else if( passwordText == null || passwordText.equals("") ) {
            passwordInput.setError("Empty password");
        } else {
            loginButton.setEnabled(false);
            UserCredentials credentials = new UserCredentials(usernameText, passwordText);

            BlueprintAPI api = new BlueprintAPI();
            api.authenticate(credentials, new APICallback<Boolean>() {
                @Override
                public void success(Boolean response) {
                    // OK - launch map view
                    setContentView(R.layout.activity_map_view);
                    Log.d("loginMsg","Login Success!");
                }

                @Override
                public void failure(String error) {
                    AlertDialog.Builder failedLoginDlg = new AlertDialog.Builder(LoginActivity.this);

                    failedLoginDlg.setTitle("Login failed!");
                    failedLoginDlg.setMessage(error);
                    failedLoginDlg.setPositiveButton("OK", null);
                    failedLoginDlg.setCancelable(true);
                    failedLoginDlg.create().show();
                    loginButton.setEnabled(true);

                    failedLoginDlg.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                }
            });
        }
    }
}
