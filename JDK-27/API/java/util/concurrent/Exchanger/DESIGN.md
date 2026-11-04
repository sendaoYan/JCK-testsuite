# java.util.concurrent.Exchanger 设计案

## 1. API 功能
两线程交换对象的同步点。构造 `()`。`exchange(V)` 阻塞直到配对的另一线程调用 exchange，返回对方对象；`exchange(V, timeout, unit)` 超时抛 `TimeoutException`。

## 2. Java 用户如何使用
- 构造：`new Exchanger<String>()`。
- 链路：线程 A `exchange(a)` 与线程 B `exchange(b)` 配对，A 得 b、B 得 a；单侧 `exchange(x, timeout)` 无配对时超时抛 TimeoutException。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/Exchanger/`、`tck/ExchangerTest.java` 覆盖双线程交换与超时。
- 场景：流水线两阶段缓冲交换（生产者/消费者交接槽位）的经典同步原语。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + exchange(V)/exchange(V, long, TimeUnit)。
2. 黑盒：主线程 + worker 线程互相交换对象（AtomicReference + CountDownLatch 确定性同步）；无配对 timed exchange 抛 TimeoutException。

实现：`ExchangerTest.java`（8 断言）。
