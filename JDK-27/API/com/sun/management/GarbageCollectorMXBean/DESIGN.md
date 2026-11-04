# com.sun.management.GarbageCollectorMXBean 测试设计

## 1. API 功能

- 接口：`com.sun.management.GarbageCollectorMXBean extends java.lang.management.GarbageCollectorMXBean`。
- 扩展方法：`getLastGcInfo()` 返回最近一次 GC 的 `GcInfo`（若该 collector 尚无记录则返回 null）。
- 继承方法：`getCollectionCount()` / `getCollectionTime()` / `getName()` 等来自父接口。

## 2. Java 用户如何使用

通过 `ManagementFactory.getGarbageCollectorMXBeans()` 拿到平台 GC MXBean，判断 `instanceof com.sun.management.GarbageCollectorMXBean` 后调用 `getLastGcInfo()`，再读取 GcInfo 的时间/内存统计。组合链路：

```
for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
    if (b instanceof com.sun.management.GarbageCollectorMXBean) {
        GcInfo gi = ((com.sun.management.GarbageCollectorMXBean) b).getLastGcInfo();
        if (gi != null) { gi.getId(); gi.getStartTime(); gi.getEndTime(); gi.getDuration(); }
    }
}
```

## 3. 开源代码实际用法调研

来源 `~/git/jdk25u-dev`（源码 + test/jdk，WebSearch 不可用则以此为准）。

- `~/git/jdk25u-dev/test/jdk/com/sun/management/GarbageCollectorMXBean/LastGCInfo.java`：`System.gc()` 后遍历 `getGarbageCollectorMXBeans()`，对 `instanceof com.sun.management.GarbageCollectorMXBean` 的 bean 调 `getLastGcInfo()`；断言 GcInfo 的 id/start/end/duration，并校验 `getMemoryUsageBeforeGc()` 与 `getMemoryUsageAfterGc()` 的 key 覆盖所有 memory pool。注意它用 `-XX:-ExplicitGCInvokesConcurrent` 保证 `System.gc()` 前台执行。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/GarbageCollectorMXBean/GarbageCollectionNotificationTest.java`：通过 `getGarbageCollectorMXBeans()` 注册 GC 通知监听，验证通知来源与 type。
- `~/git/jdk25u-dev/src/java.management/share/classes/sun/management/GarbageCollectorImpl.java`：`getLastGcInfo()` 的实现（本地通过 HotSpot 层取最近 GcInfo）。

## 4. 测试用例设计 + 自动化实现

对应 `GarbageCollectorMXBeanTest.java` 黑盒部分：

1. 平台 bean 非空且至少一个实现 `com.sun.management.GarbageCollectorMXBean`。
2. 对每个 bean 断言 `getName()!=null`、`getCollectionCount()>=0`、`getCollectionTime()>=0`。
3. `System.gc()` 后对 com.sun bean 调 `getLastGcInfo()`；若非 null，断言 GcInfo 的 `id/startTime/endTime/duration` 范围与 before/after 内存 Map 的 key 一致性。getLastGcInfo() 允许 null（不同 collector 可能尚未记录），用分支容错，不硬性要求非空。
