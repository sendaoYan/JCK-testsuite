# javax.swing.text.BoxView — DESIGN

## 1. API 功能
盒状平铺视图（`extends CompositeView`），沿主轴（X_AXIS 或 Y_AXIS）排列子视图。核心方法：
- 轴：`getAxis()/setAxis(int)`（校验 X_AXIS/Y_AXIS）。
- 尺寸：`getPreferredSpan/getMinimumSpan/getMaximumSpan`（主轴累加、次轴取子视图最大），`getInsetSpan/getLeftInset/getRightInset/getTopInset/getBottomInset/setInsets`。
- 布局：`layout(int,int)/setSize(float,float)/getWidth()/getHeight()/layoutChanged(int)/isLayoutValid`。
- 子视图定位：`getChildAllocation(int,Shape)`、`modelToView/viewToModel/getViewIndexAtPosition`。
- 对齐：`getAlignment(axis)`。

## 2. Java 用户如何使用
`new BoxView(element, View.Y_AXIS)` 后 `setParent(带 ViewFactory 的父视图)` 触发 `loadChildren` 沿主轴平铺子视图；`setSize` 布局后 `getChildAllocation(i, alloc)` 获取每个子视图矩形。子类（FlowView/TableView）重写 `layout/layoutMajorAxis/layoutMinorAxis` 定制排列。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:319`：`new BoxView(elem, View.Y_AXIS)` 用于 Section 元素。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java:821`：`class Row extends BoxView`（X_AXIS）作为段落物理行。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TableView.java:70`：`TableView extends BoxView`（Y_AXIS），行内 `TableRow extends BoxView`（X_AXIS）。

## 4. 测试用例设计（黑盒部分，`BoxViewTest.testBlackBox`）
1. 空盒：`getAxis/setAxis` 往返；空盒三尺寸均为 0；`getAlignment==0.5`；非法轴 `getPreferredSpan(2)` → IAE。
2. `setSize(100,50)` → `getWidth/getHeight` 往返。
3. 用固定跨度子视图（pref=20/min=10/max=30）+ FIXED 工厂：主轴三尺寸累加（60/30/90），次轴取最大（pref 20/min 10/max 无界）。
4. `setSize` 布局后 `getChildAllocation(0, alloc)` 返回非 null 且宽度为正。
