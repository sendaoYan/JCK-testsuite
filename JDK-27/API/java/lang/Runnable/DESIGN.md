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
