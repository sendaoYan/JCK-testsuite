# javax.swing.text.ZoneView — DESIGN

## 1. API 功能
按需加载的分区视图（`extends BoxView`）。核心方法：
- 分区参数：`getMaximumZoneSize()/setMaximumZoneSize(int)`（默认 8×1024）、`getMaxZonesLoaded()/setMaxZonesLoaded(int)`（默认 3，<1 → IllegalArgumentException）。
- 分区管理：`createZone(int,int)`（protected，内部 `class Zone extends AsyncBoxView`）、`loadChildren` 仅创建初始分区、`getViewIndexAtPosition`、`splitZone`。
- 生命周期钩子：`zoneWasLoaded(View)/unloadZone(View)/isZoneLoaded(View)`；`insertUpdate/removeUpdate` 触发分区拆分。

## 2. Java 用户如何使用
作为大文档的 BoxView 装饰器：`new ZoneView(element, View.Y_AXIS)` → 调 `setMaximumZoneSize`/`setMaxZonesLoaded` → setParent 触发初始分区；子类重写 `createZone` 定制分区实现。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ZoneView.java:382`：内部 `class Zone extends AsyncBoxView`，`load()` 按需加载、`unload()` 释放。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ZoneView.java:104`：`setMaxZonesLoaded` 校验 mzl<1 抛 IAE 并 `unloadOldZones`。

## 4. 测试用例设计（黑盒部分，`ZoneViewTest.testBlackBox`）
1. 根元素构造：`getAxis==Y_AXIS`、offsets/document。
2. `getMaximumZoneSize()==8192`、`setMaximumZoneSize(1024)` 往返。
3. `getMaxZonesLoaded()==3`、`setMaxZonesLoaded(7)` 往返、`setMaxZonesLoaded(1)` 合法、`setMaxZonesLoaded(0)` → IAE。
4. 未 setParent 时 `getViewCount()==0`（无分区创建）。
