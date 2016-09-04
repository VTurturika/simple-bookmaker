package com.victor.simplebookmaker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button startButton;
    private Button stopButton;
    private TextView info;
    private TextView userScoreView;
    private TextView androidScoreView;

    private float start = 0;
    private float finish;

    private Map<Integer, View> units = new HashMap<>();
    private List<UnitThread> unitThreads = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            moveUnit(msg.what, msg.arg1, msg.arg2 == 1);
        }
    };

    private int userBet;
    private int androidBet;
    private int userScore = 0;
    private int androidScore = 0;
    private boolean betsMade = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.buttonStart);
        stopButton = (Button) findViewById(R.id.buttonStop);
        Button resetButton = (Button) findViewById(R.id.buttonReset);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);

        info = (TextView) findViewById(R.id.info);
        userScoreView = (TextView) findViewById(R.id.userScore);
        androidScoreView = (TextView) findViewById(R.id.androidScore);

        units.put(R.id.unit1, findViewById(R.id.unit1));
        units.put(R.id.unit2, findViewById(R.id.unit2));
        units.put(R.id.unit3, findViewById(R.id.unit3));

        for(View unit: units.values()) {
            unit.setOnClickListener(this);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        finish = displayMetrics.widthPixels;

        Log.d("My Tag", "finish: " + finish);

//        final View track = findViewById(R.id.track);
//        track.post(new Runnable() {
//            @Override
//            public void run() {
//                finish = track.getRight();
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRace(0);
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonStart:
                startRace();
                break;

            case R.id.buttonStop:
                stopRace(0);
                moveUnitsToStart();
                break;

            case R.id.buttonReset:
                //resetScore();
                moveUnitsToStart();
                break;

            case R.id.unit1:
            case R.id.unit2:
            case R.id.unit3:
                createBets(v.getId());
                break;
        }
    }

    private void moveUnit(int unitId, int steps, boolean acceleration) {

        ImageView unit = (ImageView) units.get(unitId);

        float offset = unit.getX() + steps * (acceleration ? 2 : 1);
        boolean isFinish = false;

        if(offset + unit.getWidth() + unit.getLeft() > finish) {
            offset = finish - unit.getWidth() - unit.getLeft();
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
        betsMade = false;

        for (View unit : units.values()) {

            unit.animate()
                .x(start)
                .start();
        }
    }

    private void startRace() {

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        info.setText("Racing ...");

        for (View v : units.values()) {
           unitThreads.add(new UnitThread(v.getId(), handler));
        }

        for (UnitThread thread : unitThreads) {
            thread.start();
        }
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
        unitThreads.clear();
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    private void createBets(int unitId) {

        if(!betsMade) {

            betsMade = true;
            userBet = unitId;
            animateChoice(userBet);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    info.setText(getString(R.string.androidChoose));
                }
            }, 750);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Random rand = new Random();
                    int index = rand.nextInt(units.size());
                    androidBet = ((View)units.values().toArray()[index]).getId();
                    animateChoice(androidBet);
                    info.setText("Hit start!");
                    startButton.setEnabled(true);
                }
            }, 1500);

        }
    }

    private void resetScore() {
        userScore = 0;
        androidBet = 0;
        userScoreView.setText("0");
        androidScoreView.setText("0");
    }

    private void determineWinner(int firstUnit) {

        if(firstUnit == userBet && firstUnit == androidBet) {
            info.setText("Draw");
        }
        else if(firstUnit == userBet) {
            userScoreView.setText(Integer.toString(++userScore));
            info.setText("You won!");
        }
        else if(firstUnit == androidBet) {
            androidScoreView.setText(Integer.toString(++androidScore));
            info.setText("Android won!");
        }
        else {
            info.setText("Nobody win");
        }
    }

    private void animateChoice(int unitId) {

        View unit = units.get(unitId);

        Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_unit);
        unit.startAnimation(shakeAnimation);
    }
}