# Runnable — 设计方案

## 1. API 功能
`java.lang.Runnable`（接口）声明唯一抽象方法 `void run()`，是线程执行体的抽象；可传递给 `Thread` 在新线程执行，也可直接调用同步执行。

## 2. Java 用户如何使用
- 用 lambda 实现 `Runnable` 并 `new Thread(r).start()` 异步执行，或直接 `r.run()` 同步执行。
```java
Runnable r = () -> System.out.println("hi");
new Thread(r).start();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/Thread`、线程池 `Executor` 均以 `Runnable` 为基本工作单元。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Runnable")` 验证为接口，`run()` 无参返回 void。
黑盒：直接 `run()` 同步置位标志；`new Thread(r).start()` + `join()` 验证异步执行置位。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（新增维度）
恰 1 个公共方法且为唯一抽象方法 run（public、void、0 参、**无受检异常**）；非泛型；
@FunctionalInterface 注解存在。

### 2. 使用方式（新增）
三形态（lambda/静态方法引用/普通类 + 匿名类）；run() 直调在当前线程（runner==caller）且可
重复调用（无一次约束）；Thread(r) 恰好一次（latch 有界等待 + 计数==1 + worker 线程身份 +
TERMINATED + 二次 start → IllegalThreadStateException 且不重跑）；直调 run 抛异常同实例
冒泡（不经 UncaughtExceptionHandler）；new Thread((Runnable) null) 合法立即终止；daemon
线程照跑；lambda 运行时类 synthetic、双 lambda 不同实例、Object 多态持有；getId 内外一致。

### 3. 参考来源
JLS §9.8 函数式接口；Oracle javadoc Runnable/Thread.start（一次性语义）。

### 4. 测试用例设计 + 易错规范细节（新增方法）
testWhiteBoxCounts；testThreeForms；testDirectRunOnCallingThread；testThreadRunsExactlyOnce；
testDirectRunExceptionPropagation；testThreadWithNullRunnable；testDaemonRunnable；
testLambdaRuntimeFacts；testWorkerIdentity。

易错：一次性的约束来自 Thread.start（ITSE）而非 Runnable 本身；同步等待一律
CountDownLatch 有界 await（≤10s），不 sleep。
