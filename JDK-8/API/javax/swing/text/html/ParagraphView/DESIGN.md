# javax.swing.text.html.ParagraphView — DESIGN

## 1. API 功能
HTML 段落视图（`public class ParagraphView extends javax.swing.text.ParagraphView`），FlowView 段落子类，负责段落内文本流式折行（懒布局 pool）。关键方法：
- `getAlignment(int axis)`：默认 (0.5, 0.5)。
- `getBreakWeight(int axis, float pos, float len)`：宽请求 Good、零宽 Bad。
- `setParent(View)`：建立宿主关系（children 懒加载）。
- `isVisible()`：未 parenting 时 NPE（布局未建立）。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<p>` 创建。注意与 `javax.swing.text.ParagraphView` 同名需全限定名区分。单元级测试可直接构造（Metal LaF）断言属性合并与折行契约。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/ParagraphView.java`：继承 `javax.swing.text.ParagraphView`。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/CSS/bug4284162.java`：段落/流式布局回归。

## 4. 测试用例设计（黑盒，`ParagraphViewTest`：testShape + testModelLayer，24 断言）
1. testShape：superclass javax.swing.text.ParagraphView、setParent/getAttributes/isVisible/paint/span 方法存在。
2. 模型层（Metal LaF + `<div align=center><p id=p>`）：p 元素身份、未 parenting 时 getViewCount==0（children 懒加载）、属性恰 2 个（name + resolved style）。
3. 负例（精确异常）：未 parenting 时 `isVisible()` → NPE。
4. 几何：alignment (0.5f, 0.5f)；setParent(host) 往返后 getBreakWeight(X, 0, 10f)==Good、(X, 0, 0f)==Bad。
