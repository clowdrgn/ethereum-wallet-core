package util.nonceworker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by fanjl on 2017/5/27.
 */
public class NonceTimer implements Runnable{

    static final long delay = 1000 * 5 * 30;

    public static void startTimer(){

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                new NonceAcquire(),
                0,
                delay,
                TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args)  {

        Thread t1 = new Thread(new NonceTimer());
        Thread t2 = new Thread(new Test());
        t1.start();
        t2.start();
    }

    @Override
    public void run() {
        startTimer();
    }
}
