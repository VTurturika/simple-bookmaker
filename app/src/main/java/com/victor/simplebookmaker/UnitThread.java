package com.victor.simplebookmaker;

import android.os.Handler;
import android.os.Message;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class UnitThread extends Thread {

    private int unitId = 0;
    private Random generator = new Random();
    private Handler handler = null;
    private boolean isRunning = false;

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

            handler.sendMessage(Message.obtain(handler, unitId,
                    new Response(generator.nextInt(50), generator.nextBoolean())
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
}
