# javax.swing.text.LayoutQueue — DESIGN

## 1. API 功能
`LayoutQueue` 是一个文本布局任务队列，后台以单一线程（名 `text-layout`）串行执行延迟任务。方法分组：
- 构造器：`LayoutQueue()`。
- 默认队列：静态 `getDefaultQueue()`（按 AppContext 缓存单例）、`setDefaultQueue(LayoutQueue)`。
- 任务入队：`addTask(Runnable)`（首次调用启动 worker 线程，任务 FIFO 顺序执行）。

## 2. Java 用户如何使用
`LayoutQueue.getDefaultQueue().addTask(() -> { /* 延迟布局/刷新 */ });`，把不紧急的布局任务交给后台线程；必要时 `setDefaultQueue(new LayoutQueue())` 替换默认队列。由于 worker 是非 daemon 线程且会阻塞在 `wait()`，测试需中断 `text-layout` 线程让 JVM 退出。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AsyncBoxView.java`：`protected LayoutQueue getLayoutQueue() { return LayoutQueue.getDefaultQueue(); }`，并在子视图布局时 `q.addTask(...)` 调度增量布局任务。
- 同文件多处 `LayoutQueue q = getLayoutQueue(); q.addTask(flushTask);` 将刷新任务入队。

## 4. 测试用例设计（黑盒部分，`LayoutQueueTest.testBlackBox`）
1. `getDefaultQueue()` 非空且同一 AppContext 内稳定。
2. `setDefaultQueue` 生效 + 恢复原队列（不污染全局）。
3. `addTask(Runnable)` 用 CountDownLatch 等待任务执行并断言副作用（flag 置位）。
4. 同一队列两个任务按 FIFO 顺序执行（用顺序计数断言）。
5. 收尾 `interruptLayoutThreads()` 中断 `text-layout` 线程，避免非 daemon 线程挂住 jtreg。
