# javax.swing.plaf.metal.MetalTextFieldUI — DESIGN

## 1. API 功能
`MetalTextFieldUI` 是 Metal 观感下 `JTextField` 的 UI 委托（`extends BasicTextFieldUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalTextFieldUI()`、静态 `createUI(JComponent)`。
- 属性变更：`propertyChange(PropertyChangeEvent)` 直接委托 `super.propertyChange(evt)`（处理 "document"、"font"、"foreground" 等属性）。
- 继承 `BasicTextFieldUI` 的文本视图/编辑行为与 `ComponentUI` 的 `paint`、`installUI`/`uninstallUI`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JTextField` 自动挂载本委托；test/jdk 中也直接 `textField.setUI(new MetalTextFieldUI() {...})` 以自定义 `create(Element)`。直接使用时典型链：
`MetalTextFieldUI ui = new MetalTextFieldUI(); ui.installUI(tf); ui.propertyChange(evt); ...; ui.uninstallUI(tf);`。未知属性事件（如自定义 "jckProp"）应安全被忽略。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:304`：将 `"TextFieldUI"` 映射到 `MetalTextFieldUI`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/View/8156217/FPMethodCalledTest.java:112-116`：`textField.setUI(new MetalTextFieldUI() { ... })` 自定义视图创建。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTextFieldUI.java:75`：`propertyChange` 直接调用 `super.propertyChange(evt)`。

## 4. 测试用例设计（黑盒部分，`MetalTextFieldUITest.testBlackBox`）
1. 构造 `MetalTextFieldUI` 非空、`createUI` 返回本类实例。
2. `installUI(tf)` 无异常；文本域宽高为正。
3. `propertyChange(new PropertyChangeEvent(tf,"jckProp",null,null))` 无异常。
4. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`paint(g,tf)` 无异常。
5. `uninstallUI(tf)` 无异常，且文本 "hi" 保留。
