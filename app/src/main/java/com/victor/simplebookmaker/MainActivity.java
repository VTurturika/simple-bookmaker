package com.victor.simplebookmaker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button startButton;
    private Button stopButton;

    private float start = 0;
    private float finish;

    private final int UNITS_NUMBER = 3;
    private View[] units = new View[UNITS_NUMBER];
    private UnitThread[] unitThreads = new UnitThread[UNITS_NUMBER];

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UnitThread.Response response = (UnitThread.Response) msg.obj;
            moveUnit(msg.what, response.step, response.direction);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.buttonStart);
        stopButton = (Button) findViewById(R.id.buttonStop);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        units[0] = findViewById(R.id.unit1);
        units[1] = findViewById(R.id.unit2);
        units[2] = findViewById(R.id.unit3);

        final View track = findViewById(R.id.track);

        track.post(new Runnable() {
            @Override
            public void run() {
                finish = track.getRight();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRace();
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonStart:
                startRace();
                break;

            case R.id.buttonStop:
                stopRace();
                moveUnitsToStart();
                break;
        }
    }

    private void moveUnit(int unitId, int steps, boolean direction) {

        ImageView unit = (ImageView) findViewById(unitId);

        float offset = unit.getX() + steps * (direction ? 1 : -1);
        boolean isFinish = false;

        if(offset + unit.getWidth() > finish) {
            offset = finish - unit.getWidth();
            isFinish = true;
        }
        else if(offset <= start) {
            offset = -start;
        }

        unit.animate()
            .x(offset)
            .setDuration(UnitThread.FREQUENCY)
            .start();

        if(isFinish) {

            //todo analyze rate, show info dialog
            stopRace();
            moveUnitsToStart(); //todo move after close dialog
        }
    }

    private void moveUnitsToStart() {

        for (View unit : units) {

            unit.animate()
                .x(start)
                .start();
        }
    }

    private void startRace() {

        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        for (int i = 0; i < UNITS_NUMBER; i++) {
            unitThreads[i] = new UnitThread(units[i].getId(), handler);
        }

        for (UnitThread thread : unitThreads) {
            thread.start();
        }
    }

    private void stopRace() {

        for (UnitThread thread : unitThreads) {
            if(thread != null) {
                thread.stopTask();
            }
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
}