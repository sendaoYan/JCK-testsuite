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

## 5. 契约深化（本批）
- 非法 id 精确异常（实证，任务书"返回 null"猜测被推翻）：getThreadInfo(-1)/getThreadCpuTime(-1)/getThreadUserTime(-1) → IAE `Invalid thread id`；getThreadInfo(null 数组元素) → NPE。
- 单例身份：两次 getThreadMXBean() 为同一实例。
- CPU 时间生命周期：isCurrentThreadCpuTimeSupported 为真时 currentThreadCpuTime >= currentThreadUserTime >= 0 且两次采样单调不减。
- 死锁探测：两线程互锁 fixture → findMonitorDeadlockedThreads() 与 findDeadlockedThreads() 均含两个线程 id（有界轮询 10s，不 sleep 等待）。
- dumpAllThreads 返回非空且含当前线程；getAllThreadIds() ⊆ 实际存活线程。
- 版本差异：JDK-27 增加默认方法 getThreadInfo(long[],boolean,boolean,int)（4 参）；JDK-8 仅 3 参重载——双侧各断自己。
