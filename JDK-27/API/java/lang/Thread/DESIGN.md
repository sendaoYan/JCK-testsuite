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

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- 与 8 侧同套状态机/interrupt/UEH/优先级/join/sleep/holdsLock 闭环（55→302 断言）。
- **27 独有**：`threadId()`（与 getId() 同值）；toString 带 `#id` 前缀
  `"Thread[#<id>,ts-name,5,ts-group]"` 精确；构造器 null name NPE 消息为 `"'name' is null"`
  （与 setName 的 "name cannot be null" 不同）；死线程 setPriority 生效；NEW/TERMINATED 线程
  interrupt 后标志可观测（`isInterrupted()==true`）。
- **虚拟线程（Thread.ofVirtual）**：isVirtual()==true、daemon 恒 true、priority 恒 5（setPriority no-op）、
  toString 以 `VirtualThread[` 开头、await 中 WAITING、setDaemon(false)→IAE
  `"'false' not legal for virtual threads"`、start 后 setDaemon(true)→ITSE、
  unstarted→start→TERMINATED→double start ITSE、onSpinWait 可调用。
- join/sleep 负例消息与 8 相同（"timeout value is negative"/"nanosecond timeout value out of range"）。

### 使用方式
同 8 侧 latch 锚点法；虚拟线程 worker 用 Thread.ofVirtual().start + untimed latch await 断言 WAITING。

### 参考来源
JDK-27 javadoc（Thread.Builder/ofVirtual/threadId）；双侧实测探针锁定差异项（见上）。

### 测试用例设计 + 易错规范细节
- 新增 testVirtualThreads、testIdContract（threadId==getId）、testToStringPrecise（#id 格式）、
  testInterruptNewThread/testInterruptTerminated（27 可观测）、testPriorityDeadThread（27 生效）。
- 易错点：**27 构造器与 setName 的 null NPE 消息不同**；虚拟线程 setDaemon(false) 是 IAE 不是 ITSE。

- 易错点（jtreg 环境坑）：**默认 handler 只在线程组链到达未被改写的根组时被咨询**；jtreg 把测试 main 包在
  自有 MainThreadGroup（覆写 uncaughtException 且不委托）里，子线程死异常会被 harness 截走并判负。
  因此 default-routing 用例的 worker 必须锚定真实根组（getParent() 走到头），与运行环境无关。
