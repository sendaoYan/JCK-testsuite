# javax.swing.text.CompositeView — DESIGN

## 1. API 功能
抽象复合视图（`extends View`），维护子视图数组。核心方法：
- 子视图管理：`getViewCount()/getView(int)/replace(int,int,View[])/insert/append/remove`。
- 定位：`getViewIndex(int,Bias)/getViewIndexAtPosition(int)`（二分/遍历）、`getChildAllocation(int,Shape)`、`getViewAtPoint`（protected）。
- 抽象布局判定：`isBefore/isAfter`、`childAllocation`、`getViewAtPoint`（子类实现）。
- 尺寸：`getPreferredSpan/getMinimumSpan/getMaximumSpan`（默认委托子视图计算）；`setParent/loadChildren` 通过 ViewFactory 建子视图。

## 2. Java 用户如何使用
作为 BoxView 的父类，一般不直接实例化；子类（BoxView/TableView）实现 `isBefore/isAfter/getViewAtPoint/childAllocation`。用户通过 `setParent(View)` 触发 `loadChildren(ViewFactory)` 批量创建子视图，再用 `getView(i)` 遍历。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/BoxView.java`：`extends CompositeView`，实现水平/垂直平铺。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TableView.java`：`extends BoxView`（间接 extends CompositeView），按行/列组织子视图。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/FlowView.java`：内部 `class LogicalView extends CompositeView` 作为逻辑视图池。

## 4. 测试用例设计（黑盒部分，`CompositeViewTest.testBlackBox`）
1. 用 JTextPane 三段落 + StyledViewFactory，`setParent(Host)` 触发 `loadChildren`，断言每个元素一个子视图、子视图为 ParagraphView、子视图 parent 回指。
2. `getViewIndex`：0→0、len-1→最后、`getEndOffset()`→-1。
3. `getChildAllocation` 返回 Rectangle 且 childAllocation 布局生效。
4. `replace/append/remove` 增删往返后子视图计数与 parent 回指正确。
