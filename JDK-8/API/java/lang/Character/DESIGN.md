# java.lang.Character 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「读个常量」的冒烟。重点补：全部字符常量（MIN/MAX_VALUE、SIZE/BYTES、
> 基数、surrogate 上下界、code point 上下界、MIN_SUPPLEMENTARY_CODE_POINT）、digit/forDigit
> （含非法基/越界返回 -1/'\0'）、类别谓词（isDigit/isLetter/isLetterOrDigit/大小写/标题/defined）、
> 大小写转换（char 与 code point）、空白与 ISO 控制分类、Java/Unicode 标识符谓词、数值/type、
> 代理项与 code point 工具（isSurrogate/isHigh/isLow/isSurrogatePair/toCodePoint/charCount/
> isSupplementary/isBmp/isValid/high/lowSurrogate）、reverseBytes、valueOf 缓存、compare/equals/hashCode/toString。
> 断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Character`（final，char 的包装类，实现 `Comparable<Character>`/`Serializable`）。
- **常量**：MIN/MAX_VALUE、SIZE/BYTES、MIN/MAX_RADIX、MIN/MAX_SURROGATE、MIN/MAX_HIGH_SURROGATE、
  MIN/MAX_LOW_SURROGATE、MIN/MAX_CODE_POINT、MIN_SUPPLEMENTARY_CODE_POINT、各类 type 常量。
- **数字**：`digit`/`forDigit`/`getNumericValue`。
- **分类谓词**：`isDigit/isLetter/isLetterOrDigit/isUpperCase/isLowerCase/isTitleCase/isDefined/
  isWhitespace/isSpaceChar/isISOControl/isJavaIdentifierStart/isJavaIdentifierPart/
  isIdentifierIgnorable/isUnicodeIdentifierStart/isUnicodeIdentifierPart`。
- **转换/比较**：`toUpperCase/toLowerCase`、`compare/equals/hashCode/toString/valueOf/charValue/reverseBytes`。
- **代理项/code point**：`isSurrogate/isHighSurrogate/isLowSurrogate/isSurrogatePair/toCodePoint/
  charCount/isSupplementaryCodePoint/isBmpCodePoint/isValidCodePoint/highSurrogate/lowSurrogate/getType`。

## 2. Java 用户如何使用

- `Character.digit(ch, radix)` 把字符转数字（非法返回 -1）；`forDigit(n, radix)` 反向。
- `isWhitespace`/`isJavaIdentifierStart` 用于词法分析；`toCodePoint(hi, lo)` 组合代理项对。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Character.html`）：常量值、digit/forDigit 边界、谓词契约。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Character.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：代理项/code point/数值常量精确值。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Character`。

## 4. 测试用例设计（映射到 CharacterTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：MIN/MAX_VALUE、SIZE/BYTES、基数、surrogate、code point | testConstants |
| P0 | digit：十进制/十六进制/36 进制、非法基/越界返回 -1 | testDigit |
| P0 | forDigit：10/16/36、越界/非法基返回 '\0' | testForDigit |
| P0 | 类别谓词：digit/letter/letterOrDigit/大小写/title/defined | testCategory |
| P0 | 大小写转换 char 与 code point、恒等 | testCaseConversion |
| P0 | 空白/space/ISO 控制分类 | testSpace |
| P0 | Java/Unicode 标识符谓词 | testIdentifier |
| P0 | getNumericValue（含 ½=-2、x=33 等）/getType | testNumericAndType |
| P0 | 代理项：isSurrogate/isHigh/isLow/isSurrogatePair/toCodePoint | testSurrogates |
| P0 | code point：charCount/isSupplementary/isBmp/isValid/high/lowSurrogate | testCodePoint |
| P1 | reverseBytes/valueOf 缓存/charValue/compare/hashCode/toString/equals | testMisc |

## 关键规范细节（易错，已锁入断言）

- **`digit(ch, radix)` 非法 radix（<MIN_RADIX 或 >MAX_RADIX）或字符越界返回 -1**；
  **`forDigit` 非法输入返回 '\0'（NUL）**。
- **`MIN_SURROGATE=0xd800`、`MAX_SURROGATE=0xdfff`、`MIN_SUPPLEMENTARY_CODE_POINT=0x10000`、
  `MAX_CODE_POINT=0x10ffff`**。
- **`isSurrogatePair(hi, lo)` 要求 hi 是高代理项、lo 是低代理项**（顺序不可反）。
- **`charCount(0x10000)==2`、`charCount(0xffff)==1`**；`toCodePoint(0xd800,0xdc00)==0x10000`。
- **`getNumericValue` 对非数值字符返回 -1，对 ½ 返回 -2（分数）、对 'x' 返回 33（radix-36 约定）**。

## 注意事项

- headless 安全，不加 `@key`；所有输入为字面 char/code point，结果确定。
