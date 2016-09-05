package com.victor.simplebookmaker;

import android.os.Handler;
import android.os.Message;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class UnitThread extends Thread {

    private int unitId;
    private Handler handler;
    private boolean isRunning = false;

    private Random generator = new Random();
    private final int MAX = 30;
    private final int MIN = 5;

    public static int FREQUENCY = 400;

    public UnitThread(int unitId, Handler handler) {
        this.unitId = unitId;
        this.handler = handler;
    }

    @Override
    public void run() {

        isRunning = true;

        while (isRunning) {

            int nextStep = getRandomStep();
            int acceleration = nextStep == MAX ? 1 : 0;

            handler.sendMessage(Message.obtain(handler, unitId, nextStep, acceleration));

            try {
                TimeUnit.MILLISECONDS.sleep(FREQUENCY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stopTask() {
        isRunning = false;
    }

    public int getUnitId() {
        return unitId;
    }

    private int getRandomStep() {
        return generator.nextInt((MAX - MIN) + 1) + MIN;
    }
}
