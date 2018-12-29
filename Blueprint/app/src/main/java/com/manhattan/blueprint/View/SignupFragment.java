package com.manhattan.blueprint.View;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.manhattan.blueprint.R;

public class SignupFragment extends Fragment {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button signupButton;
    private TextView loginTextView;
    private View.OnClickListener onLoginClickListener;
    private View.OnClickListener onSignupClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup fragment = (ViewGroup) inflater.inflate(R.layout.fragment_signup, container, false);
        signupButton = fragment.findViewById(R.id.signupButton);
        loginTextView = fragment.findViewById(R.id.loginTextView);
        usernameInput = fragment.findViewById(R.id.usernameInput);
        passwordInput = fragment.findViewById(R.id.passwordInput);
        signupButton.setOnClickListener(onSignupClickListener);
        loginTextView.setOnClickListener(onLoginClickListener);

        return fragment;
    }

    public void setConfiguration(View.OnClickListener onSignupClickListener, View.OnClickListener onLoginClickListener) {
        this.onSignupClickListener = onSignupClickListener;
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
}
