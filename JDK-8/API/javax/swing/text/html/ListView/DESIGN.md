# javax.swing.text.html.ListView — DESIGN

## 1. API 功能
HTML 列表块视图（`public class ListView extends BlockView`），垂直排列 `<ul>`/`<ol>` 列表项并绘制项目符号。关键方法：
- `getAlignment(int axis)`：双轴 0.5（居中）。
- `getResizeWeight(int axis)`：X 轴 1。
- `paint`：绘制 list-style-type 符号。
- `getAttributes()`：含 list-style-type=disc、margin-top/bottom、以及私有 `margin-left-ltr`/`margin-right-rtl` 键（公开 CSS.Attribute.MARGIN_LEFT 查询不到）。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<ul>`/`<ol>` 创建。单元级测试可对 ul 元素直接 `new ListView(elem)`（Metal LaF）断言属性与几何模型契约。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/ListView.java`：`getAlignment` 双轴 0.5、list painter 绘制符号。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/CSS.java`：私有属性实例 margin-left-ltr/margin-right-rtl 的定义。

## 4. 测试用例设计（黑盒，`ListViewTest`：testShape + testModelLayer，26 断言）
1. testShape：superclass BlockView、getAlignment/paint 方法存在。
2. 模型层（Metal LaF + 固定 HTML）：ul 元素身份、未 parenting 时 getViewCount==0、属性非 null、恰 6 个属性（name + 5 css）。
3. 公开键：list-style-type=disc、margin-top、margin-bottom 定义。
4. 私有键：`margin-left-ltr`/`margin-right-rtl` 经属性名枚举扫描断言定义（公开 isDefined(MARGIN_LEFT) 查询失败——私有 CSS.Attribute 实例）。
5. 几何：alignment Y 0.5、resizeWeight X==1、setParent 可调用。
