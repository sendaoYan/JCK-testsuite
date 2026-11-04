# ThreadMXBean — 设计方案

## 1. API 功能
`java.lang.management.ThreadMXBean`（接口，继承 `PlatformManagedObject`）暴露线程统计与诊断：`getThreadCount`/`getPeakThreadCount`/`getTotalStartedThreadCount`/`getDaemonThreadCount`/`getAllThreadIds`；`getThreadInfo(...)`；CPU 时间；死锁检测 `findDeadlockedThreads`/`findMonitorDeadlockedThreads`；`dumpAllThreads`。

## 2. Java 用户如何使用
```java
ThreadMXBean t = ManagementFactory.getThreadMXBean();
long[] ids = t.getAllThreadIds();
ThreadInfo info = t.getThreadInfo(ids[0]);
long[] deadlocked = t.findDeadlockedThreads();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/ThreadMXBean` 系统验证线程计数、id 列表、CPU 时间、死锁检测。
- jstack/Arthas/Micrometer 用 `dumpAllThreads`/`getAllThreadIds` 采集线程。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 关键方法返回类型。
黑盒：线程计数>0、peak>=count、totalStarted>=count、daemon>=0；allThreadIds 长度==count 且含当前线程；getThreadInfo 当前线程往返；CPU 时间>=-1；无死锁；objectName 非空；dumpAllThreads 非空。
对应 `ThreadMXBeanTest.java` 白盒 + 黑盒部分。
