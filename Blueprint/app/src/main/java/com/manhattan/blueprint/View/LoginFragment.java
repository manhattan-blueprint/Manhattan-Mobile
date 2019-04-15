package com.manhattan.blueprint.View;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.ViewUtils;

public class LoginFragment extends Fragment {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private View.OnClickListener onLoginClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup fragment = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);
        loginButton = fragment.findViewById(R.id.loginButton);
        usernameInput = fragment.findViewById(R.id.usernameInput);
        passwordInput = fragment.findViewById(R.id.passwordInput);
        progressBar = fragment.findViewById(R.id.progressBar);
        loginButton.setOnClickListener(onLoginClickListener);

        return fragment;
    }

    public void setConfiguration(View.OnClickListener onLoginClickListener) {
        this.onLoginClickListener = onLoginClickListener;
    }

    public String getUsername() {
        return usernameInput.getText().toString();
    }

    public String getPassword() {
        return passwordInput.getText().toString();
    }

    public void setUsernameInvalid(String description) {
        usernameInput.setError(description);
    }

    public void setPasswordInvalid(String description) {
        passwordInput.setError(description);
    }

    public void setEnabled(Context context, boolean enabled) {
        if (enabled) {
            usernameInput.setBackground(getResources().getDrawable(R.drawable.onboarding_edittext));
            passwordInput.setBackground(getResources().getDrawable(R.drawable.onboarding_edittext));
            progressBar.setAlpha(0);
            loginButton.setAlpha(1);
        } else {
            usernameInput.setBackground(getResources().getDrawable(R.drawable.onboarding_edittext_disabled));
            passwordInput.setBackground(getResources().getDrawable(R.drawable.onboarding_edittext_disabled));
            progressBar.setAlpha(1);
            loginButton.setAlpha(0);
        }
        usernameInput.setPadding((int) ViewUtils.dpToPx(context, 20), 0, 0, 0);
        passwordInput.setPadding((int) ViewUtils.dpToPx(context, 20), 0, 0, 0);
        progressBar.setEnabled(!enabled);
    }
}
