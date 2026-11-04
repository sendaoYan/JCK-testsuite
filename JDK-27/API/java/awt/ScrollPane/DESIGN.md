# java.awt.ScrollPane 黑盒行为测试设计

## ① API 功能分组
- 常量：`SCROLLBARS_AS_NEEDED/SCROLLBARS_ALWAYS/SCROLLBARS_NEVER`。
- 构造：`ScrollPane()`、`ScrollPane(int scrollbarDisplayPolicy)`。
- 滚动条策略：`getScrollbarDisplayPolicy`。
- 布局约束：`setLayout`（final，抛 AWTError）。
- 滚动：`getVAdjustable/getHAdjustable`、`setScrollPosition/getScrollPosition`。
- 查询：`getViewportSize/getVScrollbarWidth/getHScrollbarHeight`、`setWheelScrollingEnabled/isWheelScrollingEnabled`。

## ② Java 用户用法
```java
ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
sp.add(new TextArea());
sp.setScrollPosition(0, 0);
frame.add(sp);
```
组合链路：构造 → 策略查询 → 滚动位置 set/get → add 到显示 Frame。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `ScrollPane` 默认大小 100x100，策略 AS_NEEDED（`ScrollPane.java`）。
- `ScrollPane.setLayout` 恒抛 `AWTError("ScrollPane controls layout")`（`ScrollPane.java`）。
- `ScrollPane.getScrollPosition` 由 `hAdjustable/vAdjustable` 的 value 组成（`ScrollPane.java`）。

## ④ 测试用例设计 + 自动化实现
- 常量精确值 + 构造器策略/默认尺寸 → `testBlackBox`。
- 非法策略抛 IllegalArgumentException + setLayout 抛 AWTError → `testBlackBox`。
- adjustable 非空且实现 Adjustable → `testBlackBox`。
- 滚动位置 set/get 往返 + 滚轮滚动开关 → `testBlackBox`。
- add 到显示 Frame 后 displayable → `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- 非法 policy（99）构造器抛精确 IAE；`setLayout(anything)` 抛精确 AWTError（ScrollPane 布局不可替换）。
- 无子组件时 `setScrollPosition` 抛精确 NullPointerException（child is null）。
- add 返回同一子组件实例；remove 后 compCount 归 0。
- 显示后（Xvfb）viewport 正尺寸；滚动位置钳制到 childSize-viewport 内（x=100,y=200 接受、超界压回、负值压回 0）；h/v scrollbar 尺寸非负（有 X 时 17/17）。
- `isWheelScrollingEnabled` 默认 true，可关。
