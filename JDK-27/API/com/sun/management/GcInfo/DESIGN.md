# com.sun.management.GcInfo 测试设计

## 1. API 功能

- 类：`com.sun.management.GcInfo`，实现 `javax.management.openmbean.CompositeData` 与 `CompositeDataView`；无公开构造器（只能由 `GarbageCollectorMXBean.getLastGcInfo()` 或 `GcInfo.from(CompositeData)` 获得）。
- 统计 getters：`getId()`、`getStartTime()`、`getEndTime()`、`getDuration()`（= end - start）。
- 内存快照：`getMemoryUsageBeforeGc()` / `getMemoryUsageAfterGc()` 返回 `Map<String, MemoryUsage>`（不可修改）。
- CompositeData 视图：`getCompositeType()`、`containsKey`、`containsValue`、`get`、`getAll`、`equals`、`hashCode`、`toString`、`values`。
- 转换：`toCompositeData(CompositeType)`（忽略 ct，返回内部 cdata）；静态 `from(CompositeData)`（`from(null)` 返回 null）。

## 2. Java 用户如何使用

从 `GarbageCollectorMXBean.getLastGcInfo()` 拿到 GcInfo，读取 GC 次数、起止时间、耗时，以及各内存池 GC 前后用量。组合链路：

```
GcInfo gi = ((com.sun.management.GarbageCollectorMXBean) gc).getLastGcInfo();
long id = gi.getId(); long start = gi.getStartTime(); long end = gi.getEndTime();
Map<String,MemoryUsage> before = gi.getMemoryUsageBeforeGc();
Map<String,MemoryUsage> after = gi.getMemoryUsageAfterGc();
for (String pool : before.keySet()) { MemoryUsage a = after.get(pool); ... }
CompositeData cd = gi.toCompositeData(null); GcInfo back = GcInfo.from(cd);
```

## 3. 开源代码实际用法调研

来源 `~/git/jdk25u-dev`（源码 + test/jdk，WebSearch 不可用则以此为准）。

- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/GcInfo.java`：`getDuration()` 实现为 `endTime - startTime`；before/after Map 用 `Collections.unmodifiableMap` 包装；`toCompositeData(ct)` 直接返回 `cdata`；`from(null)==null`，本地 CompositeData 走快路径返回原对象。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/GarbageCollectorMXBean/LastGCInfo.java`：遍历 before Map 的每个 pool，断言 `after.get(poolname)` 非 null，并校验 before/after key 覆盖所有 `ManagementFactory.getMemoryPoolMXBeans()`。
- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/internal/GcInfoCompositeData.java`：CompositeData 的 item 名为 `id/startTime/endTime/duration/memoryUsageBeforeGc/memoryUsageAfterGc`（注意：CompositeData 的 key 是 `"id"`，不是 javadoc 里的 `"index"`）。

## 4. 测试用例设计 + 自动化实现

对应 `GcInfoTest.java` 黑盒部分：

1. `from(null) == null`。
2. `System.gc()` 后从平台 GC MXBean 取真实 GcInfo（null 则容忍跳过）。
3. 时间/标识不变式：`id>=0`、`startTime>=0`、`endTime>=startTime`、`duration>=0`、`duration == end - start`。
4. 内存 Map：before/after 非 null；before 的 key 排序后逐项断言 value 非 null，且 `after.get(k)` 存在。
5. CompositeData 视图：`getCompositeType()!=null`、`containsKey("id")`、`get("id") instanceof Long`、`getAll({"id","startTime","endTime","duration"})` 长度 4、`toString`/`equals`/`hashCode` 可调用。
6. 往返：`gi.toCompositeData(null)` → `GcInfo.from(cd)` 后 id/start/end/duration 与原对象一致。
