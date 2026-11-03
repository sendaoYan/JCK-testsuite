# javax.swing.text.html.ObjectView — DESIGN

## 1. API 功能
HTML 对象视图（`public class ObjectView extends ComponentView`），渲染 `<object>` 元素，尝试按 `classid` 加载组件/对象；无法加载时显示一个 JLabel 提示。核心方法：
- `ObjectView(Element elem)`：唯一构造器。
- `createComponent()`：按 classid 反射实例化组件；失败返回 JLabel。
- `getComponent()`：返回已创建控件（依赖 parent/container）。
- 继承 View 的 span 查询（非法 axis 抛 IAE）。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<object>` 创建。用户也可直接 `new ObjectView(element)` 做单元级查询；要拿到已物化的控件需在视图树中取 live 实例。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/ObjectView.java`：`createComponent` 对非 Component classid 构造 JLabel。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/View/8080972/TestObjectView.java`：ObjectView 回归。

## 4. 测试用例设计（黑盒部分，`ObjectViewTest.testBlackBox`）
1. `new ObjectView(element)` 的 `getElement/getStartOffset/getEndOffset` 往返。
2. 独立实例（未物化）span == 0；非法 axis → IAE。
3. 从视图树取 live ObjectView（`classid='java.lang.String'` 无法加载为组件），`getComponent()` 为 JLabel。
4. live 实例 span `min <= pref <= max`。
