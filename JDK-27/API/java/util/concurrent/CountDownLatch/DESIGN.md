# java.util.concurrent.CountDownLatch 设计案

## 1. API 功能
一次性同步器。构造 `(int count)`。`countDown()` 递减计数；`await()`/`await(timeout, unit)` 阻塞到计数归零（超时返回 false）；`getCount()` 查询。计数不可重置、不会变负。

## 2. Java 用户如何使用
- 构造：`new CountDownLatch(n)`。
- 链路：主线程 `await()`，工作线程 `countDown()` 到零唤醒；`getCount()` 观察递减；`await(timeout)` 用于限时等待；`new CountDownLatch(0)` 立即通过。

## 3. 开源代码实际用法调研
- JDK 自用：`ForkJoinPool`/`AbstractExecutorService` 等内部以 CountDownLatch 协调终止。
- JDK 自带测试：`test/jdk/java/util/concurrent/CountDownLatch/`、`tck/CountDownLatchTest.java`。
- 框架：Spring 测试、Netty 启动/关闭流程常见「主线程等待多个 worker 就绪」模式。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + await/await(long,TimeUnit)/countDown/getCount。
2. 黑盒：countDown/getCount 递减且不越零；await 阻塞直到 countDown（用 entered/done 两个 latch 确定性同步，无裸 sleep）；await(timeout) 超时返回 false、归零返回 true；负计数抛 IllegalArgumentException。

实现：`CountDownLatchTest.java`（19 断言）。
