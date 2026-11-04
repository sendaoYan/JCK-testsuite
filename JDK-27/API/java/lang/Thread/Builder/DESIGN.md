# Thread.Builder — 设计方案

## 1. API 功能
`java.lang.Thread.Builder`（接口）是线程创建的构建器抽象（`Thread.ofPlatform()/ofVirtual()` 入口）：`name(String)`、`name(String prefix, long start)`（自动计数）、`inheritInheritableThreadLocals(boolean)`、`uncaughtExceptionHandler(...)`，终结操作 `unstarted(Runnable)`/`start(Runnable)` 返回 Thread，`factory()` 生成 `ThreadFactory`。

## 2. Java 用户如何使用
- 链式创建命名线程/线程工厂，替代直接 new Thread：
```java
ThreadFactory f = Thread.ofPlatform().name("worker-", 0).daemon().factory();
```

## 3. 开源代码实际用法调研
- JEP 444/虚拟线程引入；JDK `test/jdk/java/lang/ThreadBuilders` 验证 name 计数、inherit 开关、未捕获异常处理器；Netty 5 等以 Thread.ofPlatform() 构建线程工厂。

## 4. 测试用例设计 + 自动化实现
白盒：验证接口、各方法返回 `Thread.Builder`（链式）、`unstarted/start` 返回 `Thread`、`factory()` 返回 `ThreadFactory`。
黑盒：`start` 真正启动（join 后 TERMINATED）、`unstarted` 保持 NEW；`name(prefix, start)` 工厂连续线程名 7/8/9 递增；`uncaughtExceptionHandler` 在线程死亡时收到线程与异常；`inheritInheritableThreadLocals(false)` 关闭继承、默认继承（InheritableThreadLocal 父值，finally 中 remove 复原）。
对应 `BuilderTest.java` 白盒 + 黑盒部分。
