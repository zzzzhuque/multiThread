package com.zt.intrinsiclock;

public class AddOne implements Runnable{
    private long count;

    public AddOne() {
        count = 0;
    }

    @Override
    public synchronized void run() {
        for (int i = 0; i < 100; ++i) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count += 1;
        }
        System.out.println("count=" + count);
    }

    public void printMessage() {
        System.out.println("continue");
    }


    public static void main(String[] args) throws InterruptedException {
        AddOne addOne = new AddOne();
        Thread thread1 = new Thread(addOne, "SyncThread1");
        Thread thread2 = new Thread(addOne, "SyncThread2");
        thread1.start();
        thread2.start();
        addOne.printMessage();
    }
}
