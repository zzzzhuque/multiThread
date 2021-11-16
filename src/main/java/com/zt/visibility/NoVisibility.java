package com.zt.visibility;

public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread implements Runnable {
        @Override
        public void run() {
            while (!ready) {
                // 使当前线程从运行状态变为就绪状态
                Thread.yield();
            }
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        new ReaderThread().run();
        number = 42;
        ready = true;
    }
}
