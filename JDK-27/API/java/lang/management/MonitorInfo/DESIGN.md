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

## 5. 契约深化（本批）
- 构造边界（实证，任务书猜测被推翻）：depth == 0 且 frame == null → IAE `Parameter stackDepth is 0 but stackFrame is null`；depth < 0 配 null frame 合法（未知哨兵）；depth == 0 配非 null frame 合法。
- `from(null)` → null；from 缺 lockedMonitors/lockedStackDepth/lockedStackFrame 字段的旧版 CompositeData → depth -1、frame null（前向兼容）。
- 继承面：getClassName/getIdentityHashCode 与 LockInfo 一致；toString 含锁类名@hash。
- 真实锁链：持锁线程在 synchronized 块内 `getThreadInfo(id, true, true).getLockedMonitors()` 非空且 className 匹配（弱断言防 flaky）。
