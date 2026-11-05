# java.lang.Runnable 测试设计案（JDK-8）

> 覆盖目标：完整契约。重点补：白盒（恰 1 个抽象方法 run——函数式接口 + @FunctionalInterface
> 注解、无受检异常）、三形态（lambda/静态方法引用/普通类/匿名类）、run() 直调在当前线程执行
> （捕获 Thread.currentThread 对比）、Thread(runnable) 真跑恰好一次（latch 计数 + 二次 start →
> IllegalThreadStateException + 不重跑）、run() 抛异常直调时同实例冒泡、new Thread(null
> runnable) 合法、daemon 线程照跑、lambda 运行时类是 synthetic。

## 1. API 提供了哪些功能

- **接口** `Runnable`：唯一方法 `void run()`（无参无返回无受检异常），@FunctionalInterface；
  `Thread(Runnable)` 构造把 run 调度到新线程；`start()` 只调一次 run，
  重复 start → IllegalThreadStateException。

## 2. Java 用户如何使用

- lambda / 方法引用 / 实现类三形态构造任务；
- `r.run()` = 同步方法调用（当前线程）；`new Thread(r).start()` = 新线程一次。

```java
Runnable r = () -> doWork();
r.run();                       // 当前线程
new Thread(r).start();         // 新线程
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Runnable.html`）：run 设计契约（无异常约束、可复用）。
- **JLS §9.8** Functional Interface Types；§15.27 Lambda Expression（synthetic class 事实）。
- **OpenJDK 8u-dev** `Thread.start()` 源码（started 标志 → IllegalThreadStateException）。

## 4. 测试用例设计（映射到 RunnableTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：1 公共方法、唯一抽象、@FunctionalInterface、无受检 | testWhiteBox |
| P0 | 三形态 + 匿名类全部可 run | testThreeForms |
| P0 | run() 直调在当前线程（runner==caller） | testDirectRunOnCallingThread |
| P0 | Thread(r) 恰好一次：latch + 计数 + worker 线程身份 + TERMINATED | testThreadRunsExactlyOnce |
| P0 | 二次 start → IllegalThreadStateException 且不重跑 | testThreadRunsExactlyOnce |
| P1 | run() 抛异常：直调同实例冒泡（不经 UncaughtExceptionHandler） | testDirectRunExceptionPropagation |
| P1 | new Thread((Runnable) null) 合法并立即终止 | testThreadWithNullRunnable |
| P1 | daemon 线程照跑 | testDaemonRunnable |
| P2 | lambda 运行时类 synthetic、双 lambda 不同实例、Object 持有 | testLambdaRuntimeFacts |
| P2 | getId 内外一致 | testWorkerIdentity |
| P0 | 同步/异步执行（原有） | main 前置 + testBlackBox（27 侧） |

## 易错规范细节

- **run 可复用**：接口无"只能跑一次"约束（一次性的约束来自 Thread.start 而非 Runnable）。
- **直调 run 不切线程**：异常也沿调用栈冒泡到调用者，**不会**走 UncaughtExceptionHandler。
- **重跑保障机制**：t.start() 二次 → IllegalThreadStateException，run 不会执行第二次。
