# javax.swing.text.html.Option — DESIGN

## 1. API 功能
HTML `<option>` 表单选项（`implements Serializable`，非抽象，`extends Object`）。核心方法分组：
- 构造器：`Option(AttributeSet)`（拷贝属性，不引用原集）。
- 查询：`getValue()`（VALUE 属性，缺失时回退 label）、`getLabel()`、`isSelected()`（SELECTED 属性）、`getAttributes()`（拷贝的属性集）。
- 修改：`setLabel(String)`。
- `toString()` 返回 label。

## 2. Java 用户如何使用
`new Option(attr)` 构造后：`isSelected()` 判断是否选中、`getValue()` 取提交值、`getLabel()`/`setLabel()` 读写显示文本。典型链：`new Option(attr).getValue()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:3700`：`option = new Option(attr);` 解析 option 时用属性集构造。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLWriter.java:567-577`：`writeOption` 内 `option.getAttributes().getAttribute(HTML.Attribute.VALUE)`、`option.isSelected()`、`option.getLabel()` 序列化 option。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormView.java:842`：`Option option = model.getElementAt(i);` 遍历选项。

## 4. 测试用例设计（黑盒部分，`OptionTest.testBlackBox`）
1. 带 VALUE+SELECTED 属性：isSelected()==true、getValue()=="val1"。
2. setLabel("My Label") 后 getLabel()==toString()=="My Label"；getValue() 仍为 "val1"。
3. getAttributes() 拷贝了 VALUE 与 SELECTED。
4. 构造后修改原属性集不泄漏：getValue() 仍 "val1"。
5. 无 VALUE 属性：默认不选中；setLabel 后 getValue() 回退为 label。
