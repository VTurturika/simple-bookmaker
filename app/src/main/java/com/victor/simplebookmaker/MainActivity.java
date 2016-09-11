package com.victor.simplebookmaker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private ImageButton startPauseButton;
    private boolean startPauseMode = false; // start == true, pause == false
    private ImageButton stopButton;
    private TextView info;
    private TextView userScoreView;
    private TextView androidScoreView;

    private float finishPosition;
    private int trackMargin;
    private float pixelDensity;

    private Map<Integer, View> units = new HashMap<>();
    private List<UnitThread> unitThreads = new ArrayList<>();

    private int userBet;
    private int androidBet;
    private int userScore = 0;
    private int androidScore = 0;
    private boolean isBetsMade = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            moveUnit(msg.what, msg.arg1, msg.arg2 == 1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startPauseButton = (ImageButton) findViewById(R.id.buttonStartPause);
        stopButton = (ImageButton) findViewById(R.id.buttonStop);
        ImageButton resetButton = (ImageButton) findViewById(R.id.buttonReset);

        startPauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);

        startPauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        info = (TextView) findViewById(R.id.info);
        userScoreView = (TextView) findViewById(R.id.userScore);
        androidScoreView = (TextView) findViewById(R.id.androidScore);

        units.put(R.id.unit1, findViewById(R.id.unit1));
        units.put(R.id.unit2, findViewById(R.id.unit2));
        units.put(R.id.unit3, findViewById(R.id.unit3));

        for (View unit : units.values()) {
            unit.setOnClickListener(this);
        }
        final View track = findViewById(R.id.track);

        track.post(new Runnable() {
            @Override
            public void run() {
                finishPosition = track.getRight();
                trackMargin = track.getLeft();
            }
        });

        pixelDensity = getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRace(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!startPauseMode) {
            pauseRace();
        }
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonStartPause:

                if(startPauseMode) continueRace();
                else pauseRace();
                break;

            case R.id.buttonStop:
                stopRace(0);
                moveUnitsToStart();
                break;

            case R.id.buttonReset:
                resetScore();
                break;

            case R.id.unit1:
            case R.id.unit2:
            case R.id.unit3:
                makeBetsAndStart(v.getId());
                break;
        }
    }

    private void moveUnit(int unitId, int steps, boolean acceleration) {

        ImageView unit = (ImageView) units.get(unitId);

        float offset = unit.getX() + pixelDensity * steps * (acceleration ? 1.2f : 1);
        boolean isFinish = false;

        if(offset + unit.getWidth() + trackMargin > finishPosition) {
            offset = finishPosition - unit.getWidth() - trackMargin;
            isFinish = true;
        }

        unit.animate()
            .x(offset)
            .setDuration(UnitThread.FREQUENCY)
            .start();

        if(isFinish) {

            stopRace(unitId);
            determineWinner(unitId);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveUnitsToStart();
                }
            }, 1500);
        }
    }

    private void moveUnitsToStart() {

        info.setText(getString(R.string.playerChoose));
        isBetsMade = false;

        for (View unit : units.values()) {

            unit.animate().x(0).start();
        }

        startPauseButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    private void startRace() {

        for (View v : units.values()) {
           unitThreads.add(new UnitThread(v.getId(), handler));
        }

        for (UnitThread thread : unitThreads) {
            thread.start();
        }
    }

    private void continueRace() {

        startRace();
        startPauseMode = false;
        startPauseButton.setImageResource(R.drawable.pause);
    }

    private void pauseRace() {

        stopRace(0);
        startPauseMode = true;
        startPauseButton.setImageResource(R.drawable.start);
    }

    private void stopRace(int winnerId) {

        for (UnitThread thread : unitThreads) {
            if(thread != null) {
                thread.stopTask();
                if(winnerId == thread.getUnitId()) {
                    handler.removeMessages(thread.getUnitId());
                }
            }
        }
        startPauseMode = false;
        startPauseButton.setImageResource(R.drawable.pause);
        unitThreads.clear();
    }

    private void makeBetsAndStart(int unitId) {

        if(!isBetsMade) {

            isBetsMade = true;
            userBet = unitId;
            animateChoice(userBet);

            //emulate android thinking about choice
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText(getString(R.string.androidChoose));
                }
            }, 750);

            //animate android choice
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    units.remove(userBet);

                    Random rand = new Random();
                    int index = rand.nextInt(units.size());
                    androidBet = ((View)units.values().toArray()[index]).getId();
                    animateChoice(androidBet);

                    units.put(userBet, findViewById(userBet));
                }
            }, 1500);

            //show countdown
            int startDelay = 0;
            for (int i = 3; i >= 0 ; i--) {
                startDelay = animateCountdown(i);
            }

            //start race
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startPauseButton.setEnabled(true);
                    stopButton.setEnabled(true);
                    info.setText("");
                    startRace();
                }
            }, startDelay);

        }
    }

    private void resetScore() {
        userScore = 0;
        androidScore = 0;
        userScoreView.setText("0");
        androidScoreView.setText("0");
    }

    private void determineWinner(int firstUnit) {

        if(firstUnit == userBet && firstUnit == androidBet) {
            info.setText(R.string.drawInfo);
        }
        else if(firstUnit == userBet) {
            userScoreView.setText(Integer.toString(++userScore));
            info.setText(R.string.userWinInfo);
        }
        else if(firstUnit == androidBet) {
            androidScoreView.setText(Integer.toString(++androidScore));
            info.setText(R.string.androidWinInfo);
        }
        else {
            info.setText(R.string.drawInfo);
        }
    }

    private void animateChoice(int unitId) {

        View unit = units.get(unitId);

        Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_unit);
        unit.startAnimation(shakeAnimation);
    }

    private int animateCountdown(int count) {

        final Animation countdownAnimation = AnimationUtils.loadAnimation(this, R.anim.countdown_animation);

        int animationTime = (int) countdownAnimation.getDuration(),
            initialDelay = 2000,
            delay = initialDelay + (3 - count)*animationTime;

        final String title = (count == 0 ? "Go! " : Integer.toString(count));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                info.setText(title);
                info.startAnimation(countdownAnimation);
            }
        }, delay);

        return delay + animationTime;
    }
}