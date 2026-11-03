# javax.swing.text.MaskFormatter — DESIGN

## 1. API 功能
`MaskFormatter extends DefaultFormatter`，按掩码字符集约束输入/格式化输出（`@since 1.4`）。掩码字符：`#`数字、`'`字面量、`U`大写、`L`小写、`A`字母数字、`?`字母、`*`任意、`H`十六进制。方法分组：
- 构造器：`()`、`(String mask)`（声明抛 `ParseException`）。
- 掩码：`setMask/getMask`。
- 过滤：`setValidCharacters/getValidCharacters`、`setInvalidCharacters/getInvalidCharacters`。
- 占位：`setPlaceholder/getPlaceholder`、`setPlaceholderCharacter/getPlaceholderCharacter`（默认空格）。
- 字面量：`setValueContainsLiteralCharacters/getValueContainsLiteralCharacters`（默认 true）。
- 转换：`stringToValue(String)`（长度/字符不匹配抛 `ParseException`）、`valueToString(Object)`。

## 2. Java 用户如何使用
`new MaskFormatter("###-####")` → `valueToString("123-4567")` 得 `"123-4567"` → `stringToValue("123-4567")` 还原；非法输入（长度不符或非数字）抛 `ParseException`；`setValueContainsLiteralCharacters(false)` 后 `stringToValue("123-4567")` 返回不含字面量的 `"1234567"`；`setPlaceholderCharacter('_')`/`setPlaceholder("555-1212")` 控制不足部分补齐。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `test/jdk/javax/swing/text/bug4739057.java`：`formatter = new MaskFormatter("(###) ###-####"); formatter.setPlaceholderCharacter('#'); JFormattedTextField textField = new JFormattedTextField(formatter);`。
- `test/jdk/javax/swing/JFormattedTextField/TestSelectedTextBackgroundColor.java`：`MaskFormatter formatter = new MaskFormatter("###-##-####");`。
- 类注释示例：`MaskFormatter formatter = new MaskFormatter("###-####"); formatter.setPlaceholderCharacter('_'); System.out.println(formatter.valueToString("123"));` → `123-____`。

## 4. 测试用例设计（黑盒部分，`MaskFormatterTest.testBlackBox`）
1. 构造默认：getMask 往返、allowsInvalid=false、valueContainsLiteralCharacters=true、占位字符空格、占位串 null。
2. 含字面量往返：`valueToString("123-4567")`/`stringToValue("123-4567")` 均得 `"123-4567"`，并做 round trip。
3. 非法输入：长度不符 `"1234567"` 与非法字符 `"12a-4567"` 均抛 `ParseException`。
4. `setValueContainsLiteralCharacters(false)`：`valueToString("1234567")`→`"123-4567"`、`stringToValue("123-4567")`→`"1234567"`（剥离字面量）。
5. 占位：`setPlaceholderCharacter('_')` 后 `valueToString("123")`→`"123-____"`；`setPlaceholder("555-1212")` 后 `valueToString("123")`→`"123-1212"`。
6. 过滤：`setValidCharacters/setInvalidCharacters` 往返；invalidCharacters="9" 时 `valueToString("129-4567")` 抛 `ParseException`。
7. `setMask("(###) ###-####")` 往返。
