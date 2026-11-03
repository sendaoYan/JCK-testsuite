# ThreadInfo — 设计方案

## 1. API 功能
`java.lang.management.ThreadInfo`（类）封装线程快照：`getThreadId`/`getThreadName`/`getThreadState`、`getPriority`/`isDaemon`/`isSuspended`/`isInNative`、`getBlockedTime`/`getBlockedCount`/`getWaitedTime`/`getWaitedCount`、`getStackTrace`、`getLockInfo`/`getLockName`/`getLockOwnerId`/`getLockOwnerName`、`getLockedMonitors`/`getLockedSynchronizers`。无公开构造器，经 `ThreadMXBean.getThreadInfo` 获取。

## 2. Java 用户如何使用
```java
ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId());
StackTraceElement[] stack = info.getStackTrace();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/ThreadMXBean` 系统断言 ThreadInfo 各 getter。
- 线程 dump（jstack/Arthas）用 ThreadInfo 还原线程栈与锁。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 Object + 关键 getter 返回类型。
黑盒：经 ThreadMXBean 取当前线程的 ThreadInfo，断言 id/name/state/priority/daemon/suspended、计数非负、栈非空、toString 非空；long[] 与 maxDepth 重载往返。
对应 `ThreadInfoTest.java` 白盒 + 黑盒部分。
