package com.zt.intrinsiclock;

public class Widget {
    public synchronized void doSomething() {
        System.out.println(toString() + ": start sleep 1 second");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
