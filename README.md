# 线程安全性的概念

当多个线程访问某个类时，不管运行时环境采用何种调度方式或者线程将如何交替执行，
并且在主调代码中不需要任何额外的同步或协同，这个类都能表现出正确的行为，那么
就称这个类是线程安全的

在线程安全类中封装了必要的同步机制，因此客户端无需进一步采取同步措施

## 竞态条件

由于**不恰当的执行时序**而出现不正确的结果，也就是出现了非线程安全的**情况**，
这种情况称为竞态条件（race condition）

一种典型的竞态条件就是先检查后执行（Check-Then-Act）

## 加锁机制

加锁机制用于保证线程安全性。需要注意的是即使每一步操作都是原子的，也可能出现非
线程安全的情况。

因为各个变量之间并不是彼此独立的，而是某个变量的值会对其他变量的值产生约束。因
此，要保持状态的一致性，就需要在单个原子操作中更新所有相关的状态变量。

### 内置锁

内置锁（Intrinsic Lock）又被称为监视器锁（Monitor Lock），相当于一种互斥锁。
以关键字*synchronized*来修饰的方法就是一种横跨整个方法体的同步代码块，其中该
同步代码块的锁就是方法调用所在的对象。

```java
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
```

### 重入

内置锁可重入，当某个线程试图获得一个已经由它自己持有的锁，这个操作会成功。"重入"
意味着获取锁的操作粒度是"线程"而不是"调用"。每个锁关联了一个获取计数器和一个所有
者线程