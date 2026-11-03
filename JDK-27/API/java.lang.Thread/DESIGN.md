# Thread — 设计方案

## 1. API 功能
`java.lang.Thread` 表示执行线程，实现 `Runnable`：
- 构造：无参/`Runnable`/名字/`ThreadGroup`/栈大小/`inheritInheritableThreadLocals`
- 生命周期：`start/run/join(多个)/interrupt/isAlive/getState`
- 属性：`getName/setName/getId/threadId/getPriority/setPriority/isDaemon/setDaemon/getThreadGroup`
- 静态：`currentThread/sleep/yield/interrupted/activeCount/enumerate/holdsLock/startVirtualThread/ofPlatform/ofVirtual`

## 2. Java 用户如何使用
- 提交任务：`new Thread(r).start()`，或 `Thread.startVirtualThread(r)`。
- 等待完成：`t.join()`；协作取消：`t.interrupt()` + 任务内检查 `Thread.interrupted()`。
```java
Thread t = new Thread(() -> work(), "worker");
t.setDaemon(true);          // 后台线程，不阻塞 JVM 退出
t.start();
t.join();                    // 等待完成
```

## 3. 开源代码实际用法调研
- Netty/`ExecutorService` 内部管理线程池，`ThreadFactory` 生成带名字/daemon 的线程。
- JDK `test/jdk/java/lang/Thread` 系列验证 `join`/`interrupt`/`sleep`/`getState` 转换。
- `Thread.currentThread().getContextClassLoader()` 是框架（如 Spring、JDBC 驱动）获取加载器的标准入口。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Thread")` 验证非 final、实现 `Runnable`、`currentThread` native、`sleep` static。
黑盒：`currentThread` 自引用；名字/优先级/daemon 读写；`start/run/join` 生命周期（NEW→TERMINATED）；`join` 已结束线程立即返回；`sleep` 最小时长；中断标志设置/清除；`sleep` 被中断抛 `InterruptedException`；`holdsLock`。
对应 `<Simple>Test.java` 黑盒部分。
