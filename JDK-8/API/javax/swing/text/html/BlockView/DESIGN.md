# javax.swing.text.html.BlockView — DESIGN

## 1. API 功能
HTML 块级视图（`public class BlockView extends CompositeView`），是 HTMLEditorKit 对块级 HTML 元素（`<div>`/`<p>`/`<h1>` 等）的默认视图实现。核心方法：
- 构造器：`BlockView(Element elem, int axis)`（axis 通常 `View.Y_AXIS`）。
- `getAxis()`：返回构造时的轴。
- 伸缩权重：`getResizeWeight(int axis)` — X 轴 1（水平可伸缩）、Y 轴 0（垂直固定）；非法 axis 抛 `IllegalArgumentException`。
- `getAttributes()`：元素属性经 stylesheet 解析后的 AttributeSet（align='center' → text-align）。
- 布局/绘制：`layout/paint` 委托 CompositeView 的块布局。

## 2. Java 用户如何使用
通常不直接实例化：`HTMLEditorKit$HTMLFactory.create` 在构建视图树时创建。单元级测试可直接 `new BlockView(divElement, View.Y_AXIS)`（先 set Metal LaF）查询模型层契约：轴存储、resize weight、css 属性暴露、未布局时 span 为 0。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`（内部类 `HTMLFactory.create`）：对块级 `HTML.Tag` 构造 `new BlockView(elem, View.Y_AXIS)`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/BlockView.java`：`getResizeWeight` 的轴分支实现。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/`：块视图相关回归（CSS/HTMLDocument 系列）。

## 4. 测试用例设计（黑盒，`BlockViewTest`：testShape + testModelLayer，36 断言）
1. testShape：非接口/非枚举/非抽象/非 final、superclass BoxView、公有方法含 getPreferredSpan/getMinimumSpan/getMaximumSpan/getResizeWeight/getAlignment/getAxis/paint。
2. 模型层（Metal LaF + 固定 HTML）：div 元素身份、start/end offset 往返、Y_AXIS/X_AXIS 存储、未 parenting 时 getViewCount==0。
3. `getResizeWeight(X)==1`、`getResizeWeight(Y)==0`、`getResizeWeight(5/-1)` → IAE（精确异常）。
4. `getAttributes()` 非 null 且实例稳定；align='center' → 恰 1 个属性 text-align=center；NameAttribute 不暴露。
5. 未布局 span：preferred X/Y 均 0、min<=pref、max>=min。
6. 反例：无 align 的 div → 属性数 0。
