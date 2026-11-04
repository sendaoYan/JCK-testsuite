# javax.swing.text.html.Option — DESIGN

## 1. API 功能
HTML `<option>` 表单选项（`implements Serializable`，非抽象，`extends Object`）。核心方法分组：
- 构造器：`Option(AttributeSet)`（拷贝属性，不引用原集）。
- 查询：`getValue()`（VALUE 属性，缺失回退 label）、`getLabel()`、`isSelected()`（SELECTED 属性）、`getAttributes()`（拷贝集）。
- 修改：`setLabel(String)`；`toString()` 返回 label。

## 2. Java 用户如何使用
`new Option(attr)` 后 `isSelected()` 判断选中、`getValue()` 取提交值、`getLabel()/setLabel()` 读写显示文本。典型链：`new Option(attr).getValue()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLDocument.java`：`option = new Option(attr);` 解析 option 时构造。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLWriter.java`：`writeOption` 内用 getAttributes/isSelected/getLabel 序列化。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/FormView.java`：`Option option = model.getElementAt(i);` 遍历选项。

## 4. 测试用例设计（黑盒，`OptionTest`：testShape/testSemantics/testSerialization/testParserIntegration，53 断言）
1. testShape：implements Serializable、setLabel/getLabel/getAttributes/toString/isSelected/getValue 方法存在。
2. testSemantics：label 默认 null、VALUE 属性、SELECTED 属性置选中、toString==label、属性拷贝（构造后改原集不泄漏）、无 VALUE 时 getValue() 回退 label、setLabel 后值保持。
3. testSerialization：serialVersionUID 双侧各自断言 + roundtrip。
4. testParserIntegration：解析含 `<option>` 的 `<select>` 文档后从元素属性走 Option 语义。
