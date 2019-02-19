package com.manhattan.blueprint.Controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

import java.util.regex.Pattern;

public class OnboardingActivity extends FragmentActivity implements SurfaceHolder.Callback {
    private static final int PAGE_COUNT = 5;
    // PageIDs
    private static final int WELCOME = 0;
    private static final int LOCATION_PERMISSION = 1;
    private static final int CAMERA_PERMISSION = 2;
    private static final int LOGIN = 3;
    private static final int SIGNUP = 4;

    private final int maxUsernameLength = 16;
    private SurfaceView surface;
    private ControlledViewPager pager;
    private PermissionManager locationPermissionManager;
    private PermissionManager cameraPermissionManager;
    private LoginFragment loginFragment;
    private SignupFragment signupFragment;
    private BlueprintAPI api;
    private Point screenSize;

    private String testURL = "https://redirector.googlevideo.com/videoplayback?expire=1550602960&ratebypass=yes&mt=1550580666&fvip=3&mn=sn-x0gvfxc-vu2e%2Csn-c0q7lnsl&ei=cP5rXP6MGsLTgQe-mrDgDg&ip=185.225.17.253&ms=au%2Crdu&mv=u&source=youtube&signature=5A85872D3AAE0969ACABF6520A645A7D3A376DB8.19092F1BC74F940B65846DD6F472DE9EE17897B6&key=yt6&mime=video%2Fmp4&lmt=1541586802171970&dur=313.840&txp=5531232&pl=25&id=o-AIL4J8xHUYBsZ2-1A8L83TQQGGigoD0DXtRXViSRZ3N2&sparams=dur%2Cei%2Cid%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&itag=22&requiressl=yes&ipbits=0&mm=31%2C29&c=WEB&title=COSTA+RICA%20IN%204K%2060fps%20HDR%20(ULTRA%20HD)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        api = new BlueprintAPI(this);

        surface = findViewById(R.id.surface);
        surface.getHolder().addCallback(this);

        locationPermissionManager = new PermissionManager(LOCATION_PERMISSION, Manifest.permission.ACCESS_FINE_LOCATION);
        cameraPermissionManager = new PermissionManager(CAMERA_PERMISSION, Manifest.permission.CAMERA);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            MediaPlayer player = new MediaPlayer();
            player.setDisplay(holder);
            player.setDataSource(testURL);
            player.prepare();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnPreparedListener(mp -> player.start());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
            if (usernameText.isEmpty() || usernameText.length() > maxUsernameLength) {
                loginFragment.setUsernameInvalid(getString(R.string.invalid_username));
                return;
            } else if (!isValidPassword(passwordText)) {
                loginFragment.setPasswordInvalid(getString(R.string.invalid_password));
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
            if (usernameText.isEmpty() || usernameText.length() > maxUsernameLength) {
                signupFragment.setUsernameInvalid(getString(R.string.invalid_username));
                return;
            } else if (!isValidPassword(passwordText)) {
                signupFragment.setPasswordInvalid(getString(R.string.invalid_password));
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

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile(getResources().getString(R.string.password_regex));
        return pattern.matcher(password).matches();
    }

    private View.OnClickListener toSignupClick() {
        return v -> pager.setCurrentItem(SIGNUP);
    }

    private View.OnClickListener toLoginClick() {
        return v -> pager.setCurrentItem(LOGIN);
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
