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

## 5. 契约深化（本批）
- from(null) → null；已结束线程 id → getThreadInfo(id) 返回 null；非法 id 委托 ThreadMXBean 的 IAE。
- 真实锁链 fixture：持有者 synchronized+sleep、阻塞者 BLOCKED——getLockInfo() 非空、getLockOwnerId() == 持有者 id、getThreadState() == BLOCKED、getLockName() == 持有 monitor 类名@hash。
- 栈深契约：getStackTrace().length 与 getThreadInfo(id, maxDepth) 截断一致；maxDepth=0 → 空数组。
- dumpAllThreads 快照内当前线程的 state/suspendedInJava 弱断言（RUNNABLE/时间非负）。
- 版本差异：getPriority()/isDaemon() 为 JDK 14+——JDK-27 直接断言值；JDK-8 反射断言方法不存在。
