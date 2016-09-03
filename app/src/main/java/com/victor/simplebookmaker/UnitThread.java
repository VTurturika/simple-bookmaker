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

    public static int FREQUENCY = 250;

    public class Response {
        public int step;
        public boolean direction;

        public Response(int step, boolean direction) {
            this.step = step;
            this.direction = direction;
        }
    }

    public UnitThread(int unitId, Handler handler) {
        this.unitId = unitId;
        this.handler = handler;
    }

    @Override
    public void run() {

        isRunning = true;

        while (isRunning) {

            int nextStep = getRandomStep();
            boolean nextDirection = nextStep < 40;

            handler.sendMessage(Message.obtain(handler, unitId,
                    new Response(nextStep, nextDirection)
            ));

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

    private int getRandomStep() {

        int max = 50;
        int min = 10;
        return generator.nextInt((max - min) + 1) + min;
    }
}
