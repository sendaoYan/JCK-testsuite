# javax.swing.text.InternationalFormatter — DESIGN

## 1. API 功能
`InternationalFormatter extends DefaultFormatter`，用 `java.text.Format`（NumberFormat/DateFormat 等）做国际化格式化（`@since 1.4`）。方法分组：
- 构造器：`()`（无 Format，overwriteMode=false）、`(Format format)`。
- 格式：`setFormat(Format)/getFormat()`。
- 范围：`setMinimum/getMinimum/setMaximum/getMaximum`（Comparable；非 null 且 valueClass 未设时自动把 valueClass 设为该 Comparable 的类）。
- 转换：`valueToString(Object)`（null→""，否则 format.format）、`stringToValue(String)`（format.parseObject；越出 min/max 抛 `ParseException`）。
- 其它：`getFields(int)`、`clone()`（重置 literalMask/iterator 等瞬态字段）。

## 2. Java 用户如何使用
`new InternationalFormatter(new DecimalFormat("0.00", symbols))` → `valueToString(1234.5)` 得到 `"1234.50"` → `stringToValue("1234.50")` 还原 `Double 1234.5`；用 `setMinimum/setMaximum` 限定取值范围，越界输入在 `stringToValue` 时抛 `ParseException`；`clone()` 复制配置。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/JFormattedTextField.java`（getDefaultFormatterFactory）：
  ```java
  if (type instanceof Format) {
      return new DefaultFormatterFactory(new InternationalFormatter((Format)type));
  }
  ```
- `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSpinnerUI.java`：`if (formatter instanceof InternationalFormatter)` 后调用 `getFields(offset)` 做递增/递减。
- `test/jdk/javax/swing/JFormattedTextField/Test6462562.java`：`new InternationalFormatter(...)` 装入自定义 `JFormattedTextField` 测 `getFields/valueToString`。

## 4. 测试用例设计（黑盒部分，`InternationalFormatterTest.testBlackBox`）
1. 无参构造：format/min/max 全 null、overwriteMode=false；`(Format)` 构造保留传入 format。
2. `setFormat/getFormat` 往返；`valueToString(null)==""`、`valueToString(1234.5)=="1234.50"`。
3. `stringToValue("1234.50")` 得 `Double 1234.5`；`valueToString/stringToValue` round trip 还原。
4. 非法输入 `"abc"` 抛 `ParseException`。
5. 整数格式 + `setMinimum(0)`（自动把 valueClass 设为 Integer.class）`setMaximum(100)`：`stringToValue("50")` 得 Integer 50，`"150"`/`"-5"` 越界抛 `ParseException`。
6. `clone()`：独立实例、拷贝 format。
