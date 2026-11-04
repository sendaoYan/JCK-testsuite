# javax.swing.text.ViewFactory — DESIGN

## 1. API 功能
视图工厂接口（`public interface`）。核心方法：
- `View create(Element elem)`：为文档结构元素创建对应 View。

## 2. Java 用户如何使用
由 EditorKit 实现（BasicTextAreaUI/BasicTextFieldUI/BasicPasswordFieldUI/StyledEditorKit 的内部工厂），在 `View.loadChildren`/`setParent` 时按元素创建视图；用户可实现自定义工厂映射元素到自定义视图。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextAreaUI.java:144`：`public View create(Element elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:310`：`StyledViewFactory implements ViewFactory` 按元素名分派 LabelView/ParagraphView/BoxView/ComponentView/IconView。

## 4. 测试用例设计（黑盒部分，`ViewFactoryTest.testBlackBox`）
1. 反射：1 个方法 `create(Element)` 返回 View。
2. 具体实现：`create(null)==预置 StubView`，验证元素→视图映射契约。
