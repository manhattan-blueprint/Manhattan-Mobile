package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.PermissionManager;
import com.manhattan.blueprint.Model.UserCredentials;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.View.LoginFragment;
import com.manhattan.blueprint.View.PermissionFragment;
import com.manhattan.blueprint.View.WelcomeFragment;

public class OnboardingActivity extends FragmentActivity {
    private static final int pageCount = 4;
    private static final int locationItemID = 1;
    private static final int cameraItemID = 2;

    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private PermissionManager locationPermissionManager;
    private PermissionManager cameraPermissionManager;
    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        locationPermissionManager = new PermissionManager(locationItemID, Manifest.permission.ACCESS_FINE_LOCATION);
        cameraPermissionManager = new PermissionManager(cameraItemID, Manifest.permission.CAMERA);
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
                            locationPermissionClick());
                    return locationFragment;

                // Camera Permission
                case 2:
                    PermissionFragment cameraFragment = new PermissionFragment();
                    cameraFragment.setConfiguration("📷",
                            getString(R.string.permission_camera_title),
                            getString(R.string.permission_camera_description),
                            cameraPermissionClick());
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
            return pageCount;
        }
    }


    // OnClickHandlers
    private View.OnClickListener locationPermissionClick(){
        return v -> {
            if (locationPermissionManager.hasPermission(this)) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else {
                locationPermissionManager.requestPermission(OnboardingActivity.this);
            }
        };
    }

    private View.OnClickListener cameraPermissionClick(){
        return v -> {
            if (cameraPermissionManager.hasPermission(this)) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else {
                cameraPermissionManager.requestPermission(OnboardingActivity.this);
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

            UserCredentials credentials = new UserCredentials(usernameText, passwordText);
            BlueprintAPI api = new BlueprintAPI();
            api.authenticate(credentials, new APICallback<Boolean>() {
                @Override
                public void success(Boolean response) {
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
        case locationItemID:
            if (locationPermissionManager.hasPermission(this)){
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            } else if (!locationPermissionManager.shouldShowRequestPermissionRationale(this)) {
                // If tapped "Do not show again"
                locationPermissionManager.launchPermissionSettings(this);
            } else {
                Toast.makeText(this, "Location permissions are needed to run this application", Toast.LENGTH_LONG).show();
            }
            break;
        case cameraItemID:
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
