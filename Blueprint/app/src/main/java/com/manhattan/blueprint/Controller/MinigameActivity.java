package com.manhattan.blueprint.Controller;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.GameSession;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.ArMathUtils;
import com.manhattan.blueprint.Utils.MediaUtils;
import com.manhattan.blueprint.Utils.SpriteManager;
import com.manhattan.blueprint.Utils.ViewUtils;
import com.manhattan.blueprint.View.ModelRenderer;
import com.manhattan.blueprint.View.RoundedRectangle;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.target.SimpleTarget;
import com.warkiz.widget.IndicatorSeekBar;

import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_MOVE;

public class MinigameActivity extends AppCompatActivity {

    private Resource resourceToCollect;
    private int[] soundEffectsList;
    private int totalSounds;

    private TextView infoMessage;
    private TextView countdownIndicator;
    private GradientDrawable drawable;
    private IndicatorSeekBar progressBar;
    private View mainView;
    private View boxView;
    private View swipeIndicator;
    private Animation swipeAnimation;
    private CountDownTimer countDownTimer;
    private MediaUtils mediaUtils;
    private MediaPlayer backgroundMusic;

    private float prevX, prevY = 0; // previous coords
    private float initX, initY = 0; // initial  coords
    private float currX, currY = 0; // current  coords
    private float rotation;
    private int maxAngleError = 42;
    private float minDistance = 0.60f;
    private long countdown; // seconds
    private boolean swipeFailed = true;
    private boolean minigameReady = true;
    private boolean timerOn = false;
    private boolean gameOver = false;
    private int swipesToCollect;

    // box corners
    private int topLeft[]     = new int[2];
    private int topRight[]    = new int[2];
    private int bottomLeft[]  = new int[2];
    private int bottomRight[] = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().getDecorView().setSystemUiVisibility(
                          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_minigame);

        boxView = (View) findViewById(R.id.Minigame);
        mainView = (View) findViewById(R.id.minigameBackground);
        drawable = (GradientDrawable) getResources().getDrawable(R.drawable.ar_gesture);
        drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_neutral));
        drawable.setColor(getResources().getColor(R.color.minigame_fill_neutral));
        int screenWidth = ViewUtils.getScreenWidth(MinigameActivity.this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                (int) (screenWidth / 3.5f),
                screenWidth - 20);
        params.gravity = Gravity.CENTER;
        boxView.setLayoutParams(params);
        boxView.setForeground(drawable);
        rotation = boxView.getRotation();
        countdownIndicator = (TextView) findViewById(R.id.CounterIndicator);
        infoMessage = (TextView) findViewById(R.id.InfoMessages);
        swipeIndicator = (View) findViewById(R.id.swipeIndicator);
        countdownIndicator.bringToFront();
        infoMessage.bringToFront();

        String jsonResource = (String) getIntent().getExtras().get("resource");
        Gson gson = new GsonBuilder().create();
        resourceToCollect = gson.fromJson(jsonResource, Resource.class);

        // Configure audio
        backgroundMusic = MediaPlayer.create(getApplicationContext(), R.raw.minigame);
        backgroundMusic.setLooping(true);
        mediaUtils = new MediaUtils(backgroundMusic);

        int resId = resourceToCollect.getId();
        if (resId == 2 || resId == 4 || resId == 5 || resId == 7 || resId == 9 || resId == 10) {
            totalSounds = 7;
            soundEffectsList = new int[totalSounds];
            soundEffectsList[0] = R.raw.mine_1;
            soundEffectsList[1] = R.raw.mine_2;
            soundEffectsList[2] = R.raw.mine_2;
            soundEffectsList[3] = R.raw.mine_3;
            soundEffectsList[4] = R.raw.mine_4;
            soundEffectsList[5] = R.raw.mine_5;
            soundEffectsList[6] = R.raw.mine_6;
        } else if (resId == 1 || resId == 6) {
            totalSounds = 9;
            soundEffectsList = new int[totalSounds];
            soundEffectsList[0] = R.raw.wood_chop_1;
            soundEffectsList[1] = R.raw.wood_chop_2;
            soundEffectsList[2] = R.raw.wood_chop_3;
            soundEffectsList[3] = R.raw.wood_chop_4;
            soundEffectsList[4] = R.raw.wood_chop_5;
            soundEffectsList[5] = R.raw.wood_chop_6;
            soundEffectsList[6] = R.raw.wood_chop_7;
            soundEffectsList[7] = R.raw.wood_chop_8;
            soundEffectsList[8] = R.raw.wood_chop_9;
        } else if (resId == 3 || resId == 8) {
            totalSounds = 8;
            soundEffectsList = new int[totalSounds];
            soundEffectsList[0] = R.raw.shovel_1;
            soundEffectsList[1] = R.raw.shovel_2;
            soundEffectsList[2] = R.raw.shovel_3;
            soundEffectsList[3] = R.raw.shovel_4;
            soundEffectsList[4] = R.raw.shovel_5;
            soundEffectsList[5] = R.raw.shovel_6;
            soundEffectsList[6] = R.raw.shovel_7;
            soundEffectsList[7] = R.raw.shovel_8;
        }

        // R * 2 swipes
        // R * 1.7 + 4 seconds
        swipesToCollect = resourceToCollect.getQuantity() * 2;
        countdown = (int) (4 + resourceToCollect.getQuantity() * 1.7);
        countdownIndicator.setText(String.format("%.1f s", (float) countdown));
        progressBar = findViewById(R.id.ProgressBar);
        progressBar.bringToFront();
        progressBar.setMax(swipesToCollect);
        progressBar.setTickCount((swipesToCollect / 2) + 1);
        progressBar.setTickMarksDrawable(getDrawable(SpriteManager.getSpriteByID(resourceToCollect.getId())));

        SurfaceView surface = new SurfaceView(MinigameActivity.this);
        ModelRenderer renderer = new ModelRenderer(
                MinigameActivity.this,
                ModelRenderer.ResourceType.RAW,
                resourceToCollect.getId(),
                0.1f);
        surface.setFrameRate(60.0);
        surface.setRenderMode(ISurface.RENDERMODE_CONTINUOUSLY);
        surface.setSurfaceRenderer(renderer);

        View view = (View) findViewById(R.id.ARview);
        RelativeLayout layout = view.findViewById(R.id.model_minigame);
        layout.addView(surface);
        layout.bringToFront();
    }

    @Override
    protected void onResume() {
        super.onResume();
        backgroundMusic.setVolume(0,0);
        backgroundMusic.start();
        mediaUtils.fadeIn();

        startMinigame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaUtils.fadeOut(value -> backgroundMusic.pause());
    }

    @Override
    public void onBackPressed() {
        gameOver = true;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countdownIndicator.setText("0.0 s");
        countdownIndicator.setTextColor(getResources().getColor(R.color.red));
        boxView.setVisibility(View.INVISIBLE);
        Toast.makeText(MinigameActivity.this,
                getString(R.string.minigame_collected_none),
                Toast.LENGTH_LONG).show();
        finish();
        super.onBackPressed();
    }

    private void playTutorial() {
        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            if (session.isTutorialEnabled()) {
                infoMessage.setVisibility(View.INVISIBLE);
                progressBarTutorial();

                // disable after first play
                dao.setSession(new GameSession(
                        session.getUsername(),
                        session.getAccountType(),
                        session.getHololensIP(),
                        session.isHololensConnected(),
                        false,
                        session.getMinigames()));
            } else {
                infoMessage.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void progressBarTutorial() {
        SimpleTarget progressTarget = new SimpleTarget.Builder(this)
                .setPoint(0f, 0f)
                .setShape(new RoundedRectangle(-100f, 0f, 2000f ,120f))
                .setDescription(getString(R.string.progress_bar_tutorial))
                .setAnimation(new LinearInterpolator())
                .build();

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(500L)
                .setAnimation(new LinearInterpolator())
                .setTargets(progressTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() { }

                    @Override
                    public void onEnded() {
                        timerTutorial();
                    }
                })
                .start();
    }

    private void timerTutorial() {
        int screenWidth = ViewUtils.getScreenWidth( MinigameActivity.this);
        int timerWidth = countdownIndicator.getLayoutParams().width;
        int timerHeight = countdownIndicator.getLayoutParams().height;
        int timerTop = ((ViewGroup.MarginLayoutParams) countdownIndicator.getLayoutParams()).topMargin;

        SimpleTarget timerTarget = new SimpleTarget.Builder(this)
                .setPoint(0f, 100f)
                .setShape(new RoundedRectangle(  (screenWidth - timerWidth) / 2.0f, timerTop, timerWidth ,timerHeight))
                .setDescription(getString(R.string.timer_tutorial))
                .setAnimation(new LinearInterpolator())
                .build();

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(500L)
                .setAnimation(new LinearInterpolator())
                .setTargets(timerTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() { }

                    @Override
                    public void onEnded() {
                        swipingTutorial();
                    }
                })
                .start();
    }

    public void swipingTutorial() {
        int screenHeight = ViewUtils.getScreenHeight(MinigameActivity.this);
        int swipeWidth = boxView.getLayoutParams().width;
        int offset = 100;

        SimpleTarget swipeTarget = new SimpleTarget.Builder(this)
                .setPoint(0f, screenHeight / 2.0f + boxView.getWidth() + 570f)
                .setShape(new RoundedRectangle(-100f, (screenHeight - swipeWidth - offset) / 2.0f, 2000f, (swipeWidth + offset)))
                .setDescription(getString(R.string.swiping_tutorial))
                .setAnimation(new LinearInterpolator())
                .build();

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(500L)
                .setAnimation(new LinearInterpolator())
                .setTargets(swipeTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() { }

                    @Override
                    public void onEnded() {
                        infoMessage.setVisibility(View.VISIBLE);
                        infoMessage.setText(getString(R.string.start_when_ready));
                    }
                })
                .start();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void startMinigame() {
        getCorners();

        boxView.bringToFront();
        swipeIndicator.bringToFront();
        swipeAnimation = AnimationUtils.loadAnimation(this, R.anim.swipe_animation);
        swipeIndicator.startAnimation(swipeAnimation);
        playTutorial();

        mainView.setOnTouchListener((v, sceneMotionEvent) -> {
            if (gameOver) {
                return false;
            }
            switch (sceneMotionEvent.getAction()) {
                case ACTION_UP:
                    return motionEnded(sceneMotionEvent);

                case ACTION_DOWN:
                    return motionStarted(sceneMotionEvent);

                case ACTION_MOVE:
                    return swipeMotion(sceneMotionEvent);
            }
            return true;
        });
    }

    private void newMinigame(boolean completed, boolean newRotation) {
        minigameReady = false;
        if (completed) {
            drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_success));
            drawable.setColor(getResources().getColor(R.color.minigame_fill_success));
            boxView.setForeground(drawable);
        } else {
            drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_fail));
            drawable.setColor(getResources().getColor(R.color.minigame_fill_fail));
            boxView.setForeground(drawable);
        }
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (newRotation) {
                Random rand = new Random();
                do {
                    rotation = rand.nextInt(180) - 90;
                } while (rotation == 90);
                boxView.setRotation(rotation);
            }
            drawable.setStroke(10, getResources().getColor(R.color.minigame_outline_neutral));
            drawable.setColor(getResources().getColor(R.color.minigame_fill_neutral));
            boxView.setForeground(drawable);
            minigameReady = true;
        }, 250);
    }

    private void onSuccessfulSwipe() {
        int progress = progressBar.getProgress() + 1;
        progressBar.setProgress(progress);

        if (progress == swipesToCollect) {
            finishMinigame(true);
        }
        boxView.bringToFront();
    }

    private void finishMinigame(boolean collectedAll) {
        gameOver = true;
        countDownTimer.cancel();
        boxView.setVisibility(View.INVISIBLE);
        int quantity = progressBar.getProgress() / 2;
        if (quantity == 0) {
            countdownIndicator.setText("0.0 s");
            countdownIndicator.setTextColor(getResources().getColor(R.color.red));
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                Toast.makeText(MinigameActivity.this,
                        getString(R.string.minigame_collected_none),
                        Toast.LENGTH_LONG).show();
                finish();
            }, 2000);
            return;
        }
        InventoryItem itemCollected = new InventoryItem(resourceToCollect.getId(), quantity);
        BlueprintAPI api = new BlueprintAPI(this);
        Inventory inventoryToAdd = new Inventory(new ArrayList<>(Collections.singletonList(itemCollected)));

        api.makeRequest(api.inventoryService.addToInventory(inventoryToAdd), new APICallback<Void>() {
            @Override
            public void success(Void response) {
                if (collectedAll) {
                    countdownIndicator.setTextColor(getResources().getColor(R.color.green));
                } else {
                    countdownIndicator.setText("0.0 s");
                    countdownIndicator.setTextColor(getResources().getColor(R.color.red));
                }
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    // Show success with "You collected 5 wood", defaulting to "You collected 5 items"
                    String itemName = ItemManager.getInstance(MinigameActivity.this).getName(resourceToCollect.getId()).withDefault("items");
                    String successMsg = String.format(getString(R.string.collection_success), quantity, itemName);
                    Toast.makeText(MinigameActivity.this, successMsg, Toast.LENGTH_LONG).show();
                    finish();
                }, 1500);
            }

            @Override
            public void failure(int code, String error) {
                ViewUtils.createDialog(MinigameActivity.this,
                        getString(R.string.collection_failure_title),
                        error,
                        (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
            }
        });
    }

    // Store the (x,y) coordinates of each corner of the "gesture box"
    private void getCorners() {
        boxView.getLocationOnScreen(topLeft);

        topRight[0] = (int) (topLeft[0] + boxView.getWidth() * Math.cos(rotation * Math.PI / 180));
        topRight[1] = (int) (topLeft[1] + boxView.getWidth() * Math.sin(rotation * Math.PI / 180));

        bottomLeft[0] = (int) (topLeft[0] - boxView.getHeight() * Math.sin(rotation * Math.PI / 180));
        bottomLeft[1] = (int) (topLeft[1] + boxView.getHeight() * Math.cos(rotation * Math.PI / 180));

        bottomRight[0] = (int) (bottomLeft[0] + boxView.getWidth() * Math.cos(rotation * Math.PI / 180));
        bottomRight[1] = (int) (bottomLeft[1] + boxView.getWidth() * Math.sin(rotation * Math.PI / 180));
    }

    private boolean motionStarted(MotionEvent sceneMotionEvent) {
        if (!minigameReady) {
            return false;
        }
        if (!timerOn) {
            countDownTimer = new CountDownTimer(countdown * 1000, 100) {
                public void onTick(long millisUntilFinished) {
                    String text = String.format("%.1f s", (float) millisUntilFinished / 1000);
                    countdownIndicator.setText(text);
                }

                public void onFinish() {
                    if (!gameOver) {
                        finishMinigame(false);
                    }
                }
            }.start();
            timerOn = true;
            infoMessage.setVisibility(View.INVISIBLE);
            swipeIndicator.setVisibility(View.INVISIBLE);
            swipeIndicator.clearAnimation();
        }

        // Play a random sound from the list
        Random rand = new Random();
        int index = rand.nextInt(totalSounds);
        MediaUtils.playSoundEffect(soundEffectsList[index], getApplicationContext());

        swipeFailed = false;
        getCorners();
        initX = sceneMotionEvent.getX();
        initY = sceneMotionEvent.getY();
        prevX = initX;
        prevY = initY;
        if (ArMathUtils.outOfBounds(new int[]{(int) initX, (int) initY},
                topLeft, topRight, bottomLeft, bottomRight,
                boxView.getWidth(), boxView.getHeight())) {
            swipeFailed = true;
            newMinigame(false, true);
        }
        return true;
    }

    private boolean swipeMotion(MotionEvent sceneMotionEvent) {
        if (swipeFailed || !minigameReady) {
            return false;
        }
        currX = sceneMotionEvent.getX();
        currY = sceneMotionEvent.getY();
        double diff = ArMathUtils.getAngleError(currX, currY, prevX, prevY, rotation);
        if (ArMathUtils.outOfBounds(new int[]{(int) currX, (int) currY},
                topLeft, topRight, bottomLeft, bottomRight,
                boxView.getWidth(), boxView.getHeight())) {
            swipeFailed = true;
            newMinigame(false, true);
            return true;
        }
        if (diff > maxAngleError) {
            swipeFailed = true;
            newMinigame(false, true);
            return true;
        }
        prevX = currX;
        prevY = currY;
        return true;
    }

    private boolean motionEnded(MotionEvent sceneMotionEvent) {
        if (swipeFailed || !minigameReady) {
            return false;
        }
        currX = sceneMotionEvent.getX();
        currY = sceneMotionEvent.getY();
        if (ArMathUtils.outOfBounds(new int[]{(int) currX, (int) currY},
                topLeft, topRight, bottomLeft, bottomRight,
                boxView.getWidth(), boxView.getHeight())) {
            swipeFailed = true;
            newMinigame(false, true);
            return true;
        }
        double dist = Math.sqrt((currX - initX) * (currX - initX) + (currY - initY) * (currY - initY));
        if (dist < minDistance * boxView.getHeight()) {
            swipeFailed = true;
            newMinigame(false, true);
            return true;
        }
        onSuccessfulSwipe();
        newMinigame(true, true);
        return true;
    }
}
