package com.zt.visibility;

public class NoVisibility implements Runnable{
    private static boolean ready = false;
    private static int number = 0;

    @Override
    public void run() {
        while (!ready) {
            // 使当前线程从运行状态变为就绪状态
            System.out.println("yield");
            Thread.yield();
        }
        System.out.println("number=" + number);
    }

    public static void main(String[] args) throws InterruptedException {
        NoVisibility noVisibility = new NoVisibility();
        Thread noVisibility1 = new Thread(noVisibility, "noVisibility");
        noVisibility1.start();
        Thread.sleep(500);
        number = 42;
        ready = true;
    }
}
