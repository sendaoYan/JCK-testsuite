# javax.swing.text.AsyncBoxView.ChildLocator — DESIGN

## 1. API 功能
异步盒视图的子视图定位器（内部 public 类，`extends Object`）。核心方法：
- `getViewIndexAtPoint(float,float,Shape)`：给定坐标返回最近子视图索引（空时 -1）。
- `getChildAllocation(int,Shape)`：返回子视图分配矩形（null 分配回显 null）。
- `childChanged(ChildState)`、`setAllocation(Shape)`、`getViewIndexAtVisualOffset(float)`（protected）。
- 内部缓存 `lastAlloc`/`childAlloc` 矩形与 `lastValidOffset`。

## 2. Java 用户如何使用
一般不直接使用，由 AsyncBoxView 内部持有；测试通过 `asyncBoxView.new ChildLocator()` 构造，验证空视图下点命中返回 -1、null 分配回显 null。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AsyncBoxView.java`：`locator = new ChildLocator()`；`paint/getChildAllocation/viewToModel` 均经 locator 定位。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AsyncBoxView.java:966`：`getViewIndexAtVisualOffset` 在 n==0 时返回 `n-1`（即 -1）。

## 4. 测试用例设计（黑盒部分，`ChildLocatorTest.testBlackBox`）
1. 空 ChildLocator（无子视图）：`getViewIndexAtPoint(10,10,rect)==-1`。
2. `getChildAllocation(0, null)==null`（null 分配回显）。
