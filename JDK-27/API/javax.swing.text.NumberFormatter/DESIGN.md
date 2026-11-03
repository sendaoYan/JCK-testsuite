# javax.swing.text.NumberFormatter — DESIGN

## 1. API 功能
`NumberFormatter extends InternationalFormatter`，专为数字格式化（`@since 1.4`）。构造器默认 overwriteMode=false、allowsInvalid=true、commitsOnValidEdit=false。方法分组：
- 构造器：`()`（`NumberFormat.getNumberInstance()`）、`(NumberFormat format)`。
- 格式：`setFormat(Format)/getFormat()`。
- 转换：`valueToString(Object)`（继承 InternationalFormatter，用 format 格式化）、`stringToValue(String)`（parseObject 后按 valueClass 转换；非法输入抛 `ParseException`）。
- valueClass 特化：当 valueClass 为 Integer/Long/Float/Double/Byte/Short 时，把解析出的 Number 转成对应装箱类型。
- 继承范围校验：`setMinimum/setMaximum`（越界抛 `ParseException`）。

## 2. Java 用户如何使用
`new NumberFormatter(new DecimalFormat("0.00", symbols))` → `valueToString(1234.5)` 得 `"1234.50"` → `stringToValue("1234.50")` 得 `Double 1234.5`；`setValueClass(Integer.class)` 后 `stringToValue("42")` 得 `Integer 42`；`setMinimum(0)/setMaximum(100)` 后越界输入抛 `ParseException`；`setFormat` 切换显示格式。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/JFormattedTextField.java`（getDefaultFormatterFactory）：
  ```java
  NumberFormatter displayFormatter = new NumberFormatter();
  displayFormatter.setValueClass(type.getClass());
  NumberFormatter editFormatter = new NumberFormatter(new DecimalFormat("#.#"));
  editFormatter.setValueClass(type.getClass());
  ```
- `src/java.desktop/share/classes/javax/swing/JSpinner.java`：`NumberEditorFormatter extends NumberFormatter`，构造 `new NumberEditorFormatter(model, format)`，用 `getFormat()` 取回 `DecimalFormat`。
- `src/java.desktop/share/classes/sun/print/ServiceDialog.java`：`NumberFormatter nf = new NumberFormatter(format); ... nfto = (NumberFormatter)nf.clone();`。
- `test/jdk/javax/swing/text/bug4763466.java`：`NumberFormatter textFormatter = new NumberFormatter(new DecimalFormat("##0.00")); textFormatter.setAllowsInvalid(false); textFormatter.setValueClass(Double.class);`。

## 4. 测试用例设计（黑盒部分，`NumberFormatterTest.testBlackBox`）
1. 构造默认：getFormat 保留实例、allowsInvalid=true、overwriteMode=false、commitsOnValidEdit=false。
2. `valueToString(1234.5)=="1234.50"`、`valueToString(-12.5)=="-12.50"`；`stringToValue("1234.50")` 得 `Double 1234.5`、负数同理；round trip 还原。
3. 非法输入 `"not-a-number"` 抛 `ParseException`。
4. `setValueClass(Integer.class)` 后 `stringToValue("42")` 得 `Integer 42`。
5. 整数格式 + `setMinimum(0)/setMaximum(100)`：范围内得 Integer，`"150"` 越界抛 `ParseException`。
6. `setFormat` 换 `"0.###"` 后 `getFormat` 往返 + `valueToString(3.14)=="3.14"`。
