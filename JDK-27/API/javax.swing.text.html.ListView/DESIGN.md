# javax.swing.text.html.ListView — DESIGN

## 1. API 功能
HTML 列表块视图（`public class ListView extends BlockView`），垂直排列 `<ul>`/`<ol>` 列表项并绘制项目符号/序号。继承 BlockView 的块布局；关键方法：
- `getAlignment(int axis)`：两个轴均返回 0.5（居中）。
- `getResizeWeight(int axis)`：沿主轴（Y）的伸缩权重。
- `setParent/layout/paint`：列表项布局与绘制。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<ul>`/`<ol>` 创建。测试可在编辑器视图树中遍历获取，并查询对齐/伸缩权重/跨度。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/ListView.java`：`getAlignment` 双轴 0.5。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JFileChooser/6840086/bug6840086.java`：列表视图相关回归。

## 4. 测试用例设计（黑盒部分，`ListViewTest.testBlackBox`）
1. 从视图树取 live ListView。
2. `getAlignment(X_AXIS) == 0.5f`、`getAlignment(Y_AXIS) == 0.5f`。
3. `getResizeWeight` 有限值；span `min <= pref <= max`。
4. `setParent` 可调用、`getAttributes()` 非 null。
