# java.awt.Scrollbar 黑盒行为测试设计

## ① API 功能分组
- 常量：`HORIZONTAL/VERTICAL`。
- 构造：`Scrollbar()`、`Scrollbar(int orientation)`、`Scrollbar(int orientation, int value, int visible, int minimum, int maximum)`。
- 数值：`getValue/setValue`、`getMinimum/setMinimum`、`getMaximum/setMaximum`、`getVisibleAmount/setVisibleAmount/getVisible`。
- 增量：`getUnitIncrement/setUnitIncrement`、`getLineIncrement/setLineIncrement`、`getBlockIncrement/setBlockIncrement`、`getPageIncrement/setPageIncrement`、`setValues`。
- 状态：`getOrientation/setOrientation`、`getValueIsAdjusting/setValueIsAdjusting`。
- 事件：`addAdjustmentListener/removeAdjustmentListener/getAdjustmentListeners`。

## ② Java 用户用法
```java
Scrollbar s = new Scrollbar(Scrollbar.VERTICAL, 0, 10, 0, 100);
s.setValue(20);
s.addAdjustmentListener(e -> {});
```
组合链路：构造 → 数值/增量 setter → 监听器 → add 到显示 Frame。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Scrollbar()` 委托 `this(VERTICAL, 0, 10, 0, 100)`（`Scrollbar.java`）。
- `Scrollbar.setValues` 一次性设置 value/visible/min/max（`Scrollbar.java`）。
- `Scrollbar.getVisible` 是 `getVisibleAmount` 别名（`Scrollbar.java`）。

## ④ 测试用例设计 + 自动化实现
- 常量精确值 + 默认值/可见量 → `testBlackBox`。
- 三构造器方向与数值 → `testBlackBox`。
- 数值/增量/方向/调整状态 setter-getter 往返 + setValues → `testBlackBox`。
- AdjustmentListener 管理 → `testBlackBox`。
- add 到显示 Frame 后 displayable → `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- 非法 orientation（5/-1）在 3 个构造器与 setOrientation 均抛精确 IAE（illegal scrollbar orientation）。
- ctor clamp：value<min 抬到 min；value>max-visible 压回；min>max 时 max 抬到 min+1。
- 增量钳制：`setUnitIncrement(0/-5)`、`setBlockIncrement(0/-5)` 不抛、钳到 1；默认 unit=1、block=visible。
- `setValues` 一站式 clamp 矩阵（同上三条规则且 min 提升会连带移动 value）；`setValue` clamp 到 [min, max-visible]。
- 默认 value 0/min 0/max 100/visible 10；getVisible 与 getVisibleAmount 别名。
- 同一 AdjustmentListener 重复注册计数 2；程序化 setValue 不派发 AdjustmentEvent。
