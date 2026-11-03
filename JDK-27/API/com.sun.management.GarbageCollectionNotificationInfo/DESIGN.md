# com.sun.management.GarbageCollectionNotificationInfo 测试设计

## 1. API 功能

- 构造器：`GarbageCollectionNotificationInfo(String gcName, String gcAction, String gcCause, GcInfo gcInfo)`（gcName/gcAction/gcCause 为 null 时抛 NullPointerException，gcInfo 允许为 null）。
- 查询 getters：`getGcName()` / `getGcAction()` / `getGcCause()` / `getGcInfo()`。
- 常量：`GARBAGE_COLLECTION_NOTIFICATION = "com.sun.management.gc.notification"`，是 GC 通知的 type。
- 转换：实现 `javax.management.openmbean.CompositeDataView`，`toCompositeData(CompositeType)` 返回 CompositeData 表示；静态 `from(CompositeData)` 反向还原（`from(null)` 返回 null）。
- 内部包私有构造器 `GarbageCollectionNotificationInfo(CompositeData)` 用于 `from()` 从远端 CompositeData 还原。

## 2. Java 用户如何使用

GC 通知场景：`GarbageCollectorMXBean` 在 GC 结束时发 Notification，userData 里的 CompositeData 用 `GarbageCollectionNotificationInfo.from(cd)` 还原，再读取各字段。典型组合链路：

```
Notification notif = ...;
if (notif.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
    CompositeData cd = (CompositeData) notif.getUserData();
    GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from(cd);
    info.getGcName(); info.getGcAction(); info.getGcCause(); info.getGcInfo();
}
```

也常直接 new 一个对象再 `toCompositeData(null)` 得到 CompositeData（用于本地构造/测试）。

## 3. 开源代码实际用法调研

来源为 `~/git/jdk25u-dev`（JDK-27 对应实现）与 `~/git/jdk25u-dev/test/jdk`（WebSearch 不可用时以 JDK 源码+自带测试为准，不凭记忆编造）。

- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/GarbageCollectionNotificationInfo.java`：
  - 构造器对三个 String 参数做 null 检查并抛 `NullPointerException`；gcInfo 不检查、可为 null。
  - `from(CompositeData cd)`：`cd == null` 返回 null；`cd instanceof GarbageCollectionNotifInfoCompositeData` 时走本地快路径返回原对象，否则走包私有构造器重建。
  - `toCompositeData(CompositeType ct)`：忽略 ct，直接返回构造时生成的 `cdata`（懒对象，不立即物化）。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/GarbageCollectorMXBean/GarbageCollectionNotificationContentTest.java`：接收真实 GC 通知后用 `GarbageCollectionNotificationInfo.from((CompositeData) n.getUserData())` 还原并断言 gcName/gcAction/gcCause/gcInfo。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/GarbageCollectorMXBean/GarbageCollectionNotificationTest.java`：校验通知 type 等于 `GARBAGE_COLLECTION_NOTIFICATION` 常量。

## 4. 测试用例设计 + 自动化实现

从上述推导的断言场景（对应 `GarbageCollectionNotificationInfoTest.java` 黑盒部分）：

1. 构造 + getters：`new GarbageCollectionNotificationInfo("young","end of minor GC","System.gc()",null)` 后断言四个 getter（含 `getGcInfo()==null`）。
2. null 参数边界：三个 String 参数分别传 null，断言抛 `NullPointerException`（源码明确行为）。
3. CompositeDataView 往返：`n.toCompositeData(null)` 非 null → `from(cd)` 非 null → 断言 gcName/gcAction/gcCause 往返一致、gcInfo 仍为 null。
4. `from(null) == null`。
5. 常量值：`GARBAGE_COLLECTION_NOTIFICATION == "com.sun.management.gc.notification"`。

白盒部分保留原有结构断言（非接口、实现 CompositeDataView、方法名集合、常量字段），并入 `testWhiteBox()`。
