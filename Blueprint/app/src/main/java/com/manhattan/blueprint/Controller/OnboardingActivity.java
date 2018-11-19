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
import android.view.View;
import android.widget.Toast;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.LoginManager;
import com.manhattan.blueprint.Model.PermissionManager;
import com.manhattan.blueprint.Model.UserCredentials;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.View.ControlledViewPager;
import com.manhattan.blueprint.View.LoginFragment;
import com.manhattan.blueprint.View.PermissionFragment;
import com.manhattan.blueprint.View.WelcomeFragment;

public class OnboardingActivity extends FragmentActivity {
    private static final int PAGE_COUNT = 4;
    private static final int LOCATION_PERMISSION_ID = 1;
    private static final int CAMERA_PERMISSION_ID = 2;

    private ControlledViewPager pager;
    private PermissionManager locationPermissionManager;
    private PermissionManager cameraPermissionManager;
    private LoginFragment loginFragment;
    private BlueprintAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        api = new BlueprintAPI();

        locationPermissionManager = new PermissionManager(LOCATION_PERMISSION_ID, Manifest.permission.ACCESS_FINE_LOCATION);
        cameraPermissionManager = new PermissionManager(CAMERA_PERMISSION_ID, Manifest.permission.CAMERA);
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
                    welcomeFragment.setConfiguration(v -> pager.setCurrentItem(pager.getCurrentItem() + 1));
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
                    loginFragment.setConfiguration(loginClick());
                    return loginFragment;

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
    private View.OnClickListener permissionClick(PermissionManager permissionManager){
        return v -> {
            if (permissionManager.hasPermission(this)) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else {
                permissionManager.requestPermission(OnboardingActivity.this);
            }
        };
    }

    private View.OnClickListener loginClick(){
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

            api.authenticate(new UserCredentials(usernameText, passwordText), new APICallback<Boolean>() {
                @Override
                public void success(Boolean response) {
                    LoginManager loginManager = new LoginManager(OnboardingActivity.this);
                    loginManager.setLoggedIn(true);

                    // Launch Map View
                    Intent toMapView = new Intent(OnboardingActivity.this, MapViewActivity.class);
                    startActivity(toMapView);
                    finish();
                }

                @Override
                public void failure(String error) {
                    AlertDialog.Builder failedLoginDlg = new AlertDialog.Builder(OnboardingActivity.this);
                    failedLoginDlg.setTitle("Login failed.");
                    failedLoginDlg.setMessage(error);
                    failedLoginDlg.setCancelable(true);
                    failedLoginDlg.create().show();
                    failedLoginDlg.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                }
            });
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
        case LOCATION_PERMISSION_ID:
            if (locationPermissionManager.hasPermission(this)){
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else if (!locationPermissionManager.shouldShowRequestPermissionRationale(this)) {
                // If tapped "Do not show again"
                locationPermissionManager.launchPermissionSettings(this);
            } else {
                Toast.makeText(this, "Location permissions are needed to run this application", Toast.LENGTH_LONG).show();
            }
            break;
        case CAMERA_PERMISSION_ID:
            if (cameraPermissionManager.hasPermission(this)){
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else if (!cameraPermissionManager.shouldShowRequestPermissionRationale(this)) {
                // If tapped "Do not show again"
                cameraPermissionManager.launchPermissionSettings(this);
            } else {
                Toast.makeText(this, "Camera permissions are needed to run this application", Toast.LENGTH_LONG).show();
            }
            break;
        }
    }
}
