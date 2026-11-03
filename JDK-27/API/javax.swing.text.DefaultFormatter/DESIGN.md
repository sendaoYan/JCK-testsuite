# javax.swing.text.DefaultFormatter — DESIGN

## 1. API 功能
`DefaultFormatter extends JFormattedTextField.AbstractFormatter implements Cloneable, Serializable`，把任意对象按 `toString` 格式化、按 valueClass 的 String 构造器还原（`@since 1.4`）。方法分组：
- 构造器：`DefaultFormatter()`（默认 overwriteMode=true、allowsInvalid=true）。
- 编辑行为：`setCommitsOnValidEdit/getCommitsOnValidEdit`、`setOverwriteMode/getOverwriteMode`、`setAllowsInvalid/getAllowsInvalid`。
- 值类：`setValueClass/getValueClass`。
- 转换：`valueToString(Object)`（null→""，否则 toString）、`stringToValue(String)`（valueClass 有 String 构造器则反射构造，否则原样返回 String，构造器抛异常则抛 `ParseException`）。
- 拷贝：`clone()`（浅拷贝，重置 navigationFilter/documentFilter/replaceHolder）。

## 2. Java 用户如何使用
`new DefaultFormatter()` → `setValueClass(Integer.class)` → `stringToValue("42")` 得到 `Integer 42` → `valueToString(obj)` 得到文本；`clone()` 复制一个配置好的实例供多个字段使用；`setAllowsInvalid/setCommitsOnValidEdit/setOverwriteMode` 控制编辑期校验与提交。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/JFormattedTextField.java`：`new AbstractFormatter()` 内部使用 `DefaultFormatter` 作为无类型默认。
- `test/jdk/javax/swing/JFormattedTextField/8080972/TestDefaultFormatter.java`：直接实例化 `DefaultFormatter` 测试 `valueToString/stringToValue/clone`。
- `test/jdk/javax/swing/JFormattedTextField/bug4886538.java`：通过 `DefaultFormatterFactory` 取出 `DefaultFormatter` 调用 `clone()` 验证字段行为。

## 4. 测试用例设计（黑盒部分，`DefaultFormatterTest.testBlackBox`）
1. 构造器默认值：overwriteMode=true、allowsInvalid=true、commitsOnValidEdit=false、valueClass=null。
2. setCommitsOnValidEdit/setOverwriteMode/setAllowsInvalid/setValueClass 往返。
3. `valueToString(null)==""`、`valueToString("hello")=="hello"`。
4. `setValueClass(Integer.class)` 后 `stringToValue("42")` 得到 `Integer 42`；`setValueClass(Object.class)`（无 String 构造器）时 `stringToValue("plain")` 原样返回 String。
5. 用自定义 `Boom`（String 构造器抛异常）验证 `stringToValue` 抛 `ParseException`。
6. `clone()`：独立实例、同类、拷贝 valueClass；修改 clone 不影响原对象。
