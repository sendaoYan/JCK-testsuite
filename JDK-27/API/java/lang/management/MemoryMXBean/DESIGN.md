# MemoryMXBean — 设计方案

## 1. API 功能
`java.lang.management.MemoryMXBean`（接口，继承 `PlatformManagedObject`）暴露堆/非堆内存：`getHeapMemoryUsage()`/`getNonHeapMemoryUsage()`、`getObjectPendingFinalizationCount()`、`isVerbose()`/`setVerbose()`、`gc()`、`getTotalGcCpuTime()`。

## 2. Java 用户如何使用
```java
MemoryMXBean m = ManagementFactory.getMemoryMXBean();
long used = m.getHeapMemoryUsage().getUsed();
m.gc();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/MemoryMXBean` 断言 used/committed/max 关系与 pending finalization 非负。
- Micrometer 用其采集堆/非堆用量指标。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 各方法返回类型。
黑盒：heap/non-heap 的 used>=0、committed>=used、max>=-1；pending>=0；objectName 非空；totalGcCpuTime>=-1；setVerbose 生效并恢复；gc 可调用（不断效果）。
对应 `MemoryMXBeanTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- 使用不变式（弱断言）：heap/nonHeap 的 used <= committed、init >= -1、max >= -1 或 >= used。
- gc() 调用无异常；getObjectPendingFinalizationCount() >= 0。
- setVerbose 往返 true/false 回读一致。
- ObjectName 精确 `java.lang:type=Memory`。
- 通知路径弱断言：addNotificationListener/removeNotificationListener 往返不抛（不断言实际触发，防 flaky）。
- 版本差异：JDK-27 getTotalGcCpuTime() >= 0（JDK 16+ 默认方法，直接断言）。
