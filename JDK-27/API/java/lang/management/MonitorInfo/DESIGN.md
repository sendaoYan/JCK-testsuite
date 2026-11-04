# MonitorInfo — 设计方案

## 1. API 功能
`java.lang.management.MonitorInfo`（继承 `LockInfo`）额外记录持有锁时的栈深度/帧：`getLockedStackDepth()`、`getLockedStackFrame()`；构造 `(String, int, int, StackTraceElement)`；静态 `from(CompositeData)`。

## 2. Java 用户如何使用
```java
for (MonitorInfo m : threadInfo.getLockedMonitors()) {
    String cls = m.getClassName();
    StackTraceElement frame = m.getLockedStackFrame();
}
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/ThreadMXBean` 断言锁监视器的 className/depth/frame。
- 线程 dump 工具用 MonitorInfo 还原监视器锁持有位置。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 LockInfo + 构造器 + 两个 getter。
黑盒：构造后继承的 getClassName/getIdentityHashCode 与自身 getLockedStackDepth/getLockedStackFrame 往返。
对应 `MonitorInfoTest.java` 白盒 + 黑盒部分。
