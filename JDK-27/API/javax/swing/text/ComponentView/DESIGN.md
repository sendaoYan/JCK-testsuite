# javax.swing.text.ComponentView — DESIGN

## 1. API 功能
内嵌 Swing 组件视图（`extends View`）。核心方法：
- 组件获取：`getComponent()`（未 setParent 前 null，setParent 后返回嵌入组件）。
- 尺寸：`getPreferredSpan/getMinimumSpan/getMaximumSpan/getAlignment`（委托给包装组件的 Invalidator）。
- 映射：`modelToView/viewToModel`（组件视为单字符，与 IconView 相同的中点二分逻辑）。
- 内部 `class Invalidator extends Container` 缓存子组件尺寸。

## 2. Java 用户如何使用
通过 StyledEditorKit 的 Component 元素创建（`StyleConstants.setComponent` 后插入 styled document 生成 "component" 元素）；用户 `new ComponentView(componentElement)` 后 setParent 到含 Container 的父视图再 `getComponent()` 取得 JLabel 等嵌入组件。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:321`：Component 元素 `new ComponentView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ComponentView.java`：`Invalidator` 在构造时缓存 child 尺寸，`getPreferredSpan` 返回 `c.getPreferredSize()`。

## 4. 测试用例设计（黑盒部分，`ComponentViewTest.testBlackBox`）
1. JTextPane + JLabel("embedded")：component 元素名、getElement/offsets。
2. setParent 前 `getComponent()==null`，setParent(Host) 后 `getComponent()==label`。
3. X/Y 跨度非负且 min<=pref<=max；`getAlignment` 在 [0,1]。
4. `modelToView` start→左边缘/end→右边缘、零宽；越界 → BadLocationException；`viewToModel` 左半→start、右半→end。
