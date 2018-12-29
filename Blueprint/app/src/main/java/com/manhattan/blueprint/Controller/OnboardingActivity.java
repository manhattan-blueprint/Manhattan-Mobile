package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Managers.LoginManager;
import com.manhattan.blueprint.Model.Managers.PermissionManager;
import com.manhattan.blueprint.Model.UserCredentials;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.View.ControlledViewPager;
import com.manhattan.blueprint.View.LoginFragment;
import com.manhattan.blueprint.View.PermissionFragment;
import com.manhattan.blueprint.View.SignupFragment;
import com.manhattan.blueprint.View.WelcomeFragment;

public class OnboardingActivity extends FragmentActivity {
    private static final int PAGE_COUNT = 5;
    // PageIDs
    private static final int WELCOME = 0;
    private static final int LOCATION_PERMISSION = 1;
    private static final int CAMERA_PERMISSION = 2;
    private static final int LOGIN = 3;
    private static final int SIGNUP = 4;

    private ControlledViewPager pager;
    private PermissionManager locationPermissionManager;
    private PermissionManager cameraPermissionManager;
    private LoginFragment loginFragment;
    private SignupFragment signupFragment;
    private BlueprintAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        api = new BlueprintAPI(this);

        locationPermissionManager = new PermissionManager(LOCATION_PERMISSION, Manifest.permission.ACCESS_FINE_LOCATION);
        cameraPermissionManager = new PermissionManager(CAMERA_PERMISSION, Manifest.permission.CAMERA);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                // Welcome
                case 0:
                    WelcomeFragment welcomeFragment = new WelcomeFragment();
                    welcomeFragment.setConfiguration(welcomeClick());
                    return welcomeFragment;

                // Location Permission
                case 1:
                    PermissionFragment locationFragment = new PermissionFragment();
                    locationFragment.setConfiguration("🗺",
                            getString(R.string.permission_location_title),
                            getString(R.string.permission_location_description),
                            permissionClick(locationPermissionManager));
                    return locationFragment;

                // Camera Permission
                case 2:
                    PermissionFragment cameraFragment = new PermissionFragment();
                    cameraFragment.setConfiguration("📷",
                            getString(R.string.permission_camera_title),
                            getString(R.string.permission_camera_description),
                            permissionClick(cameraPermissionManager));
                    return cameraFragment;

                // Login Fragment
                case 3:
                    loginFragment = new LoginFragment();
                    loginFragment.setConfiguration(loginClick(), toSignupClick());
                    return loginFragment;
                case 4:
                    signupFragment = new SignupFragment();
                    signupFragment.setConfiguration(signupClick(), toLoginClick());
                    return signupFragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }


    // OnClickHandlers
    private View.OnClickListener welcomeClick() {
        return v -> {
            // Restore state where user left off
            if (locationPermissionManager.hasPermission(OnboardingActivity.this) && cameraPermissionManager.hasPermission(OnboardingActivity.this)) {
                // To login
                pager.setCurrentItem(LOGIN);
            } else if (locationPermissionManager.hasPermission(OnboardingActivity.this)) {
                // To camera permissions
                pager.setCurrentItem(CAMERA_PERMISSION);
            } else {
                // To location permissions
                pager.setCurrentItem(LOCATION_PERMISSION);
            }
        };
    }

    private View.OnClickListener permissionClick(PermissionManager permissionManager) {
        return v -> {
            if (permissionManager.hasPermission(this)) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else {
                permissionManager.requestPermission(OnboardingActivity.this);
            }
        };
    }

    private View.OnClickListener loginClick() {
        return v -> {
            String usernameText = loginFragment.getUsername();
            String passwordText = loginFragment.getPassword();

            // Validate user input
            if (usernameText.isEmpty()) {
                loginFragment.setUsernameInvalid("Empty Username");
                return;
            } else if (passwordText.isEmpty()) {
                loginFragment.setPasswordInvalid("Empty Password");
                return;
            }

            api.login(new UserCredentials(usernameText, passwordText), new APICallback<Void>() {
                @Override
                public void success(Void response) {
                    LoginManager loginManager = new LoginManager(OnboardingActivity.this);
                    loginManager.login(usernameText);

                    // Launch Map View
                    Intent toMapView = new Intent(OnboardingActivity.this, MapViewActivity.class);
                    startActivity(toMapView);
                    finish();
                }

                @Override
                public void failure(int code, String error) {
                    AlertDialog.Builder failedLoginDlg = new AlertDialog.Builder(OnboardingActivity.this);
                    failedLoginDlg.setTitle("Login failed");
                    failedLoginDlg.setMessage(error);
                    failedLoginDlg.setCancelable(true);
                    failedLoginDlg.setPositiveButton(R.string.positive_response, (dialog, which) -> dialog.dismiss());
                    failedLoginDlg.create().show();
                }
            });
        };
    }

    private View.OnClickListener signupClick() {
        return v -> {
            String usernameText = signupFragment.getUsername();
            String passwordText = signupFragment.getPassword();

            // Validate user input
            if (usernameText.isEmpty()) {
                signupFragment.setUsernameInvalid("Empty Username");
                return;
            } else if (passwordText.isEmpty()) {
                signupFragment.setPasswordInvalid("Empty Password");
                return;
            }

            api.signup(new UserCredentials(usernameText, passwordText), new APICallback<Void>() {
                @Override
                public void success(Void response) {
                    LoginManager loginManager = new LoginManager(OnboardingActivity.this);
                    loginManager.login(usernameText);

                    // Launch Map View
                    Intent toMapView = new Intent(OnboardingActivity.this, MapViewActivity.class);
                    startActivity(toMapView);
                    finish();
                }

                @Override
                public void failure(int code, String error) {
                    AlertDialog.Builder failedLoginDlg = new AlertDialog.Builder(OnboardingActivity.this);
                    failedLoginDlg.setTitle("Sign up failed");
                    failedLoginDlg.setMessage(error);
                    failedLoginDlg.setCancelable(true);
                    failedLoginDlg.setPositiveButton(R.string.positive_response, (dialog, which) -> dialog.dismiss());
                    failedLoginDlg.create().show();
                }
            });

        };
    }

    private View.OnClickListener toSignupClick() {
        return v -> {
            pager.setCurrentItem(SIGNUP);
        };
    }

    private View.OnClickListener toLoginClick() {
        return v -> {
            pager.setCurrentItem(LOGIN);
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
                if (locationPermissionManager.hasPermission(this)) {
                    // If we somehow already have camera permissions, we can skip it
                    int offset = cameraPermissionManager.hasPermission(this) ? 2 : 1;
                    pager.setCurrentItem(pager.getCurrentItem() + offset);
                } else if (!locationPermissionManager.shouldShowRequestPermissionRationale(this)) {
                    // If tapped "Do not show again"
                    locationPermissionManager.launchPermissionSettings(this);
                } else {
                    Toast.makeText(this, R.string.permission_location_description, Toast.LENGTH_LONG).show();
                }
                break;
            case CAMERA_PERMISSION:
                if (cameraPermissionManager.hasPermission(this)) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1);
                } else if (!cameraPermissionManager.shouldShowRequestPermissionRationale(this)) {
                    // If tapped "Do not show again"
                    cameraPermissionManager.launchPermissionSettings(this);
                } else {
                    Toast.makeText(this, R.string.permission_camera_description, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
