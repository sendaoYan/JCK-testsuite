# javax.swing.text.View — DESIGN

## 1. API 功能
文本视图体系抽象基类（`implements SwingConstants`）。核心常量：`X_AXIS=0/Y_AXIS=1`；`BadBreakWeight=0/GoodBreakWeight=1000/ExcellentBreakWeight=2000/ForcedBreakWeight=3000`；方向 `NORTH/SOUTH/EAST/WEST`。方法分组：
- 层级/模型：`getParent()/getContainer()/getDocument()/getViewFactory()/getElement()/getStartOffset()/getEndOffset()/getAttributes()`。
- 尺寸需求：`getPreferredSpan/getMinimumSpan/getMaximumSpan/getAlignment/getResizeWeight`。
- 子视图：`getViewCount()/getView(int)/getChildAllocation()/getViewIndex(...)`。
- 映射：`modelToView()/viewToModel()/getNextVisualPositionFrom()`；绘制 `paint()`；断裂 `breakView()/createFragment()/getBreakWeight()`；通知 `preferenceChanged()/insertUpdate()/removeUpdate()/changedUpdate()`；`isVisible()/getToolTipText()`。

## 2. Java 用户如何使用
通常不直接实例化（抽象），通过子类（PlainView/BoxView/…）与 ViewFactory 工作；用户自定义视图需实现 `getPreferredSpan/paint/modelToView/viewToModel` 四个抽象方法，其余方法提供默认行为（`getViewCount=0`、`breakView/createFragment` 返回 this、`getAlignment=0.5`、`getResizeWeight=0`）。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java:1430`：内部 `class RootView extends View`，重写 `getView/getViewCount/getPreferredSpan/paint/modelToView/viewToModel` 包装真实根视图。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java:821`：内部 `class Row extends BoxView`，作为 FlowView 的物理行子视图。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ComponentView.java`：`class Invalidator extends Container` 作为子组件容器，展示 getContainer 重写模式。

## 4. 测试用例设计（黑盒部分，`ViewTest.testBlackBox`）
1. 通过匿名子类验证四抽象方法实现契约 + 默认可见/无子视图/`getView(0)==null`/`getChildAllocation==null`/`getViewIndex==-1`。
2. 默认 `getAlignment==0.5`、`getResizeWeight==0`、`getMinimumSpan/getMaximumSpan==getPreferredSpan`（weight 0）。
3. `breakView/createFragment` 默认返回 this；`getBreakWeight` 依据 len 与 span 比较返回 Good/Bad。
4. 无父时 `getContainer/getViewFactory/getToolTipText/getViewIndex(x,y)` 为 null/-1；`preferenceChanged` 无父为 no-op。
5. 通过真实 JTextArea 验证 `modelToView/viewToModel` 单调性与越界 `BadLocationException`。
