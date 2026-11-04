# Thread.UncaughtExceptionHandler — 设计方案

## 1. API 功能
`java.lang.Thread.UncaughtExceptionHandler`（接口）在线程因未捕获异常终止时回调 `uncaughtException(Thread t, Throwable e)`；`Thread.set/getUncaughtExceptionHandler` 设置每线程处理器，`Thread.setDefaultUncaughtExceptionHandler` 设置全局默认（`ThreadGroup` 实现本接口并向上委托）。

## 2. Java 用户如何使用
- 统一记录线程崩溃：
```java
Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(t.getName(), e));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/Thread/UncaughtExceptionHandlerTest.java` 验证每线程/默认处理器触发与恢复；Netty（FastThreadLocalInstaller）、RxJava、Spring 的 `TaskExecutor` 均注册默认处理器防崩溃静默。

## 4. 测试用例设计 + 自动化实现
白盒：验证接口、`uncaughtException(Thread, Throwable)` public abstract 返回 void、`ThreadGroup` 实现本接口。
黑盒：lambda 处理器直接调用收到线程/异常对；`setUncaughtExceptionHandler` 后线程抛异常终止，join 后处理器收到该线程与异常（`getUncaughtExceptionHandler` 往返）；`setDefault/getDefaultUncaughtExceptionHandler` round-trip + finally 恢复；每线程处理器优先于默认（死亡线程不咨询默认）；经 getter 直接调用默认处理器收到 (thread, throwable)。注：不再让线程在自定义默认下死亡——jtreg harness 自身的未捕获异常记账会误判测试失败。
对应 `UncaughtExceptionHandlerTest.java` 白盒 + 黑盒部分。
