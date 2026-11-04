# javax.swing.text.DateFormatter — DESIGN

## 1. API 功能
`DateFormatter extends InternationalFormatter`，用 `java.text.DateFormat` 做日期/字符串互相转换的国际化格式化器（`@since 1.4`）。方法分组：
- 构造器：`DateFormatter()`（等价 `DateFormatter(DateFormat.getDateInstance())`）、`DateFormatter(DateFormat format)`。
- 格式：`setFormat(DateFormat)`；继承 `getFormat()`。
- 继承自 InternationalFormatter 的转换：`valueToString(Object)`、`stringToValue(String)`（非法输入抛 `ParseException`）、`setMinimum/getMinimum/setMaximum/getMaximum`、`clone()`、`getFields(int)`。
- 继承自 DefaultFormatter：`setAllowsInvalid/setValueClass/setCommitsOnValidEdit/setOverwriteMode` 等。

## 2. Java 用户如何使用
`new DateFormatter(new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT))` → `valueToString(date)` 得到显示文本 → 用户编辑后 `stringToValue(text)` 还原 `Date`；`getFormat()` 取回底层 `DateFormat`；配合 `JFormattedTextField` 安装格式化器使用。日期解析失败（如 `stringToValue("not-a-date")`）抛 `ParseException`。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/JFormattedTextField.java`（getDefaultFormatterFactory）：
  ```java
  if (type instanceof DateFormat) {
      return new DefaultFormatterFactory(new DateFormatter((DateFormat)type));
  }
  if (type instanceof Date) {
      return new DefaultFormatterFactory(new DateFormatter());
  }
  ```
- `src/java.desktop/share/classes/javax/swing/JSpinner.java`：内部类 `DateEditorFormatter extends DateFormatter`，构造时 `DateFormatter formatter = new DateEditorFormatter(model, format);`，用 `getFormat()` 取回 `SimpleDateFormat`。
- `test/jdk/javax/swing/JFormattedTextField/Test6462562.java`：`DateFormatter fmt = new DateFormatter(format);` 后用自定义 `JFormattedTextField` 测试行为。

## 4. 测试用例设计（黑盒部分，`DateFormatterTest.testBlackBox`）
1. 用 `SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)` + 固定 `GMT` 时区与固定 `Date`（2024-01-15 00:00 GMT）构造，验证 `getFormat()==` 传入实例。
2. `valueToString(fixedDate)` 精确等于 `"2024-01-15"`；`stringToValue("2024-01-15")` 还原出相等的 `Date`；再做一次显式 round trip。
3. `valueToString(null)` 返回 `""`。
4. 非法输入 `stringToValue("not-a-date")` 抛 `ParseException`。
5. `setFormat` 换 `"dd/MM/yyyy"` 后 `getFormat` 往返 + 新格式输出 `"15/01/2024"`。
