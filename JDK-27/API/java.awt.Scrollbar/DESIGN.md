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
