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

# 对象的共享

## 可见性

可见性是指一个线程对共享值的修改可被其他线程感知。但在缺少同步的情况下，编译器、
处理器以及运行时对语句的执行顺序未知，比如第五行代码会比第四行代码先执行（这种现
象称为重排序）

这就使得下面的程序可能会持续循环，因为读线程可能永远看不到ready的值。也可能会输
出0，因为可能ready先赋值，而number还没有被赋值

```java
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
```

## volatile

java内存模型要求，变量的读取操作和写入操作都必须是原子操作，但对于非volatile
类型的long和double变量，JVM允许将64位的读操作或写操作分解为两个32位的操作。
所以在多线程中使用共享且可变的long、double是不安全的，除非用volatile声明，或者
用锁保护起来。

加锁机制既可以确保可见性又可以确保原子性，而**volatile变量只能确保可见性和部分有序性，
不保证原子性**

第一：使用volatile关键字会强制将修改的值立即写入主存；

第二：使用volatile关键字的话，当线程2进行修改时，会导致线程1的工作内存中缓存变量stop
的缓存行无效（反映到硬件层的话，就是CPU的L1或者L2缓存中对应的缓存行无效）；

第三：由于线程1的工作内存中缓存变量stop的缓存行无效，所以线程1再次读取变量stop的值时
会去主存读取。

通常来说，使用volatile必须具备以下2个条件：

1）对变量的写操作不依赖于当前值

2）该变量没有包含在具有其他变量的不变式中

实际上，这些条件表明，可以被写入 volatile 变量的这些有效值独立于任何程序的状态，包括
变量的当前状态。

事实上，**我的理解就是上面的2个条件需要保证操作是原子性操作，才能保证使用volatile关键字
的程序在并发时能够正确执行。 所以状态标记量使用volatile很合适**

```java
volatile boolean flag = false;
 
while(!flag){
    doSomething();
}
 
public void setFlag() {
    flag = true;
}
```

# 发布（Publish）和逸出（Escape）

发布是指使对象能够在当前作用域之外的代码中使用，当某个不该发布的对象发布时，称为
逸出

```java
class UnsafeStates {
    private String[] states = new String[] {
            "AK", "AL", ...
    };
    
    public String[] getStates() {
        return states;
    }
}
```

get的本意是获取私有变量的值，但如果得到的是引用，则这个私有变量有被窜改的风险。

关于*当ThisEscape发布EventListener时，也隐含地发布了ThisEscape实例本身，因为在
这个内部类的实例中包含了对ThisEscape实例的隐含引用。*

在https://segmentfault.com/q/1010000007900854中有较完善的解释，因为内部类
在构造的时候会把外部类的this作为隐式参数传进去，这就使得外部类还没有初始化完成，
内部类可以拿着这个隐式this改变外部类的属性。

规避方法是使用一个私有的构造函数和一个公共的静态工厂方法（Factory Method），从
而避免不正确的构造过程

# 线程封闭

如果仅在单线程内访问数据，就不需要同步，这种技术称为线程封闭（Thread Confinement）
java提供栈封闭和ThreadLocal类实现线程封闭。

Ad-hoc是指维护线程封闭性的职责完全由程序实现来承担，一般不使用。