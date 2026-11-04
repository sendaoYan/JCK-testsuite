# java.awt.ScrollPane 行为测试设计

## 1. API 功能
构造：`ScrollPane()`/`(int scrollbarPolicy)`，常量 SCROLLBARS_AS_NEEDED=0/ALWAYS=1/NEVER=2；布局：`setLayout`（拒绝）；子组件：`add`；滚动：`getScrollPosition/setScrollPosition`、`getHScrollbarHeight/getVScrollbarWidth`、`getViewportSize`；`setWheelScrollingEnabled`；h/v Adjustable。

## 2. Java 用户如何使用
```java
ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
sp.add(bigComponent);
sp.setScrollPosition(100, 200);
```
构造策略 → 布局拒绝 → 加子组件 → 显示后滚动钳制。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/ScrollPane.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractScrollPane()`）。
对应 `ScrollPaneTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 非法 policy（99）构造器抛精确 IAE；`setLayout(anything)` 抛精确 AWTError（ScrollPane 布局不可替换）。
- 无子组件时 `setScrollPosition` 抛精确 NullPointerException（child is null）。
- add 返回同一子组件实例；remove 后 compCount 归 0。
- 显示后（Xvfb）viewport 正尺寸；滚动位置钳制到 childSize-viewport 内（x=100,y=200 接受、超界压回、负值压回 0）；h/v scrollbar 尺寸非负（有 X 时 17/17）。
- `isWheelScrollingEnabled` 默认 true，可关。
