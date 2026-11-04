# java.util.concurrent.CountDownLatch 设计案

## 1. API 功能
一次性同步器。构造 `(int count)`（负数 IAE）。`countDown()` 递减计数；`await()`/`await(timeout, unit)` 阻塞到计数归零（超时返回 false）；`getCount()` 查询。计数不可重置、不会变负。

## 2. Java 用户如何使用
- 构造：`new CountDownLatch(n)`。
- 链路：主线程 `await()`，工作线程 `countDown()` 到零唤醒；`getCount()` 观察递减；`await(timeout)` 用于限时等待；`new CountDownLatch(0)` 立即通过；`await(0,unit)`/`await(负,unit)` 立即判定（归零 true / 未归零 false）。

## 3. 开源代码实际用法调研
- JDK 自用：`ForkJoinPool`/`AbstractExecutorService` 等内部以 CountDownLatch 协调终止。
- JDK 自带测试：`test/jdk/java/util/concurrent/CountDownLatch/`、`tck/CountDownLatchTest.java`。
- 框架：Spring 测试、Netty 启动/关闭流程常见「主线程等待多个 worker 就绪」模式。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致。

## 4. 测试用例设计 + 自动化实现（PASS 45 断言）
场景：
1. 白盒：类 + await/await(long,TimeUnit)/countDown/getCount。
2. 黑盒：countDown/getCount 递减且不越零；await 阻塞直到 countDown（entered/done latch 确定性，无裸 sleep）；await(timeout) 超时 false、归零 true；负计数 IAE。
3. 契约深化（新增）：负计数 IAE 消息精确 `count < 0`（实测）；await 中断 → InterruptedException（daemon + interrupt + join 有界，count 不变）；await(0,unit)/await(负,unit) 立即判定矩阵；归零后 untimed await 立即返回；await(timeout, null unit) → NPE（只断类型）；一次 countDown 同时释放 3 个等待者；等待期间归零 → timed await true（结果与调度次序无关）；toString 含 "Count = N"。

实现：`CountDownLatchTest.java`。线程纪律：全部新线程 daemon、等待 ≤5s 有界、join(ms) 有界、无 sleep 轮询。
