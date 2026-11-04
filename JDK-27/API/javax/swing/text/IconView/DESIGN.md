# javax.swing.text.IconView — DESIGN

## 1. API 功能
内嵌图标视图（`extends View`）。核心方法：
- 尺寸：`getPreferredSpan`（X=图标宽、Y=图标高）、`getMinimumSpan/getMaximumSpan`、`getAlignment`（X=0.5、Y=1.0 底部对齐）。
- 映射：`modelToView/viewToModel`（图标视为单字符：start→左边缘、end→右边缘、中点二分映射）。
- 绘制 `paint` 在分配矩形内绘制 Icon；`getToolTipText`。

## 2. Java 用户如何使用
通过 StyledEditorKit 的 Icon 元素创建（`StyleConstants.setIcon` 后插入 styled document 生成 "icon" 元素）；用户 `new IconView(iconElement)` 查询图标几何，或依赖 factory 在 JTextPane 中渲染。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:323`：Icon 元素 `new IconView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ComponentView.java`：modelToView/viewToModel 与 IconView 采用相同的中点二分逻辑。

## 4. 测试用例设计（黑盒部分，`IconViewTest.testBlackBox`）
1. JTextPane + 16×12 ImageIcon：icon 元素名、getElement/offsets/document。
2. `getPreferredSpan(X)==16、Y==12`；`getAlignment(Y)==1.0`、`getAlignment(X)==0.5`。
3. `modelToView` start→左边缘/end→右边缘、零宽；越界 → BadLocationException。
4. `viewToModel` 左半→start(Forward)、右半→end(Backward)。
