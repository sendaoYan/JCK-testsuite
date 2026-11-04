# javax.swing.text.html.ObjectView — DESIGN

## 1. API 功能
HTML 对象视图（`public class ObjectView extends ComponentView`），渲染 `<object>` 元素，按 `classid` 反射实例化组件。核心方法：
- `ObjectView(Element elem)`：唯一公有构造器。
- `createComponent()`：按 classid 实例化；非 Component/缺失/加载失败 → 红色 "??" JLabel 兜底；`<param>` 传播为 bean 属性。
- `getComponent()`：返回已创建控件（依赖 parent/container）。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<object>` 创建；也可 `new ObjectView(element)` 单元级查询。JDK-9+ 上 `ComponentView#createComponent` 为包私有，反射调用需 `--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/ObjectView.java`：`createComponent` 反射加载 classid 指定的类并实例化。
- `~/git/jdk8u/jdk/test/javax/swing/text/View/8080972/TestObjectView.java`：ObjectView 回归。

## 4. 测试用例设计（黑盒，`ObjectViewTest`：testShape + testModelLayer，17 断言）
1. testShape：superclass ComponentView、createComponent 方法存在。
2. 模型层（Metal LaF + 固定 HTML）：object 元素身份、offset 往返、未 parenting 时 getViewCount==0、span 契约。
3. createComponent 兜底（反射）：非 Component classid / 缺失 classid → JLabel 兜底，文本 "??" 且前景为红色；param 子元素传播为 bean 属性（JDK-27 侧 tag 带 `--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED`）。
