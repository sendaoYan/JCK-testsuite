# Thread.State — 设计方案

## 1. API 功能
`java.lang.Thread.State`（final 枚举）是线程生命周期状态机：`NEW`、`RUNNABLE`、`BLOCKED`（等监视器锁）、`WAITING`（wait/join 无限等待）、`TIMED_WAITING`（带超时等待）、`TERMINATED`；由 `Thread.getState()` 报告，`values()/valueOf` 为枚举机制。

## 2. Java 用户如何使用
- 诊断/监控线程状态、实现线程池健康检查：
```java
if (worker.getState() == Thread.State.BLOCKED) { alert(); }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/Thread/StateTest.java` 以监视器与 join 观察全部六态；JFR/Arthas/JConsole 等诊断工具按 State 分桶展示线程。

## 4. 测试用例设计 + 自动化实现
白盒：验证枚举、继承 Enum、恰 6 个常量且名字精确。
黑盒：`valueOf` 往返、非法名抛 IAE、`compareTo` 按声明序；NEW（未启动）/RUNNABLE（当前线程）/TERMINATED（join 后）；用一把监视器锁确定性制造 WAITING（`lock.wait()`）、TIMED_WAITING（`lock.wait(60s)`）、BLOCKED（主线程持锁、第三个线程抢锁），经 `Thread.onSpinWait` 有界自旋观察后 notifyAll/释放并 join 三线程全部 TERMINATED（全程无 sleep）。
对应 `ThreadStateTest.java` 白盒 + 黑盒部分。
