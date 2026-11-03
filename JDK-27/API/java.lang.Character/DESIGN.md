# java.lang.Character 测试方案（字符判定/大小写/数字转换部分）

## 1. API 功能（按方法分组）

- 常量：`MIN_VALUE`/`MAX_VALUE`/`SIZE`/`BYTES`、`MIN_RADIX`/`MAX_RADIX`、代理区/码点上下界常量。
- 字符判定：`isDigit`、`isLetter`、`isLetterOrDigit`、`isUpperCase`、`isLowerCase`、`isTitleCase`、`isWhitespace`、`isSpaceChar`、`isISOControl`、`isDefined`（char 与 int 码点重载）。
- 大小写转换：`toUpperCase`/`toLowerCase`（char 与 int 重载）、`toTitleCase`。
- 数字转换：`digit(ch, radix)`/`digit(cp, radix)`、`forDigit(int, radix)`、`getNumericValue(ch)`/`getNumericValue(cp)`。
- 工具：`valueOf`/`charValue`/`compare`/`compareTo`/`toString`/`hashCode`/`reverseBytes`/`getType`/`getName`/`codePointOf`/`charCount`/`isSurrogate*`/`toCodePoint` 等。

## 2. Java 用户如何使用

- 解析数字：`Character.digit('F', 16) == 15`；回写 `Character.forDigit(15, 16) == 'f'`。
- 判定/转换：`Character.isLetter(ch)`、`Character.toUpperCase(ch)`；`Character.isDigit('５')`（全角数字也判定）。
- 组合链路：`forDigit(digit('F',16),16)` 往返；`codePointOf(getName(0x41))` 往返；`toUpperCase(0x10400)==0x10428 的反向`。

## 3. 开源代码实际用法调研

来源：JDK 测试（权威）。

- `test/jdk/java/lang/Character/CharCheck.java`：逐字符 `isDigit`/`isLetter`/`digit` 等分类/转换断言。
- `test/jdk/java/lang/Character/Latin1Digit.java`、`TestNegativeCodepoint.java`：`Character.digit` 的码点与负数边界。
- `test/jdk/java/lang/Character/Supplementary.java`、`UnicodeSpec.java`：增补平面字符与代理对处理。
- JDK 内部实现（`StringLatin1`/`StringUTF16`、`Integer.parseInt` 等）大量依赖 `Character.digit` 做进制解析。

## 4. 测试用例设计 + 自动化实现

- 正常链路：常量 → `digit`/`forDigit` → `isDigit`/`isLetter`/`isLetterOrDigit`/`isUpperCase`/`isLowerCase`/`isTitleCase` → `toUpperCase`/`toLowerCase`/`toTitleCase` → `getNumericValue`/`getType` → 代理对/码点助手 → `compare`/`compareTo`/`valueOf`。
- 边界/异常：`digit` 非法 radix/非数字返回 `-1`；`forDigit` 越界返回 `'\0'`；`codePointOf` 未知名抛 `IllegalArgumentException`；`getNumericValue('½')==-2`、`'@'==-1`。
- 深化不变量/边界：增补平面大小写（U+10400/U+10428）、titlecase（U+01C5→U+01C4/U+01C6）；`isWhitespace` 排除 NBSP(U+00A0)/FIGURE SPACE(U+2007) 但 `isSpaceChar` 包含；全角数字 `digit('５',10)==5`；罗马数字 `getNumericValue('Ⅷ')==8`。
- 自动化实现：`CharacterTest.testWhiteBox()`（反射：final/继承/接口/`digit` 返回 int/`codePointOf` 形态）+ `testBlackBox()`（`testConstants`、`testDigit`、`testForDigit`、`testCategory`、`testCaseConversion`、`testSpace`、`testIdentifier`、`testNumericAndType`、`testSurrogates`、`testCodePoint`、`testMisc`、`testCodepointNumericAndCompare`、`testSupplementaryCase`、`testWhitespaceEdge`、`testNumericEdge`）。
