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

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **digit/forDigit 全矩阵**：'0'-'9'/a-z/A-Z × radix 10/16/36 逐字符；radix 越界（37/1/0/负数）→ -1 不抛异常；全角数字/字母（U+FF10/FF19/FF16/FF21）参与进制解码；forDigit 0-9/0-15 精确字符 + 越界（16,16、35,35、0,1）→ '\0'；**每个 radix 2..36 两端点 digit↔forDigit 往返（70 断言）**。
- **getNumericValue 矩阵**：ASCII 全表 + 全角（'１'/'９'/'Ａ'）+ 罗马数字（Ⅰ=1/Ⅷ=8/Ⅻ=12/Ⅿ=1000）+ '〇'=0 + **CJK 表意文字 '一'==-1（非数值）** + 负例 '$'/'+'。
- **类别矩阵**：a-z/A-Z/0-9 四谓词组合全表；getType 用命名常量断（UPPERCASE_LETTER/DECIMAL_DIGIT_NUMBER/CONNECTOR_PUNCTUATION/CURRENCY_SYMBOL/OTHER_LETTER/OTHER_NUMBER/LETTER_NUMBER/INITIAL_QUOTE_PUNCTUATION/CONTROL/SURROGATE/SPACE_SEPARATOR）；**isAlphabetic vs isLetter 分叉**（罗马数字 Ⅰ：alphabetic true / letter false）。
- **isWhitespace vs isSpaceChar 分叉矩阵（两侧一致）**：U+0020/U+2000/U+2003/U+1680/U+200A/U+205F/U+2028/U+2029/U+3000 两者皆 true；**U+00A0/U+2007/U+202F（无断行空格）isSpaceChar true 但 isWhitespace false**；U+0009-000D、U+001C-001F isWhitespace true 但 isSpaceChar false；软连字符两者 false。
- **isISOControl 矩阵**：0x00-0x1F 全 true（32 断言）、0x7F true、**0x80-0x9F 全 true（32 断言）**、0x20/0xA0/'a' false。
- **大小写转换矩阵**：ASCII a-z/A-Z 全表（含 toTitleCase）、0x20-0x40 标点数字不变表；**'ß'（sharp s）char 版返回自身（不能扩成 "SS"——String 的事）**、'ı'→'I'、希腊 α↔Α、U+D7FF 不变。
- **代理对/码点套件**："\ud834\udd1e"（U+1D11E）codePointAt/before（含孤立代理）、codePointCount==1（含 mixed "a\ud834\udd1eb"==3）、offsetByCodePoints、char[] 重载（含 limit 形态）、toChars 往返（BMP 单元/增广双单元/0x10FFFF）；MIN/MAX_HIGH/LOW_SURROGATE 精确 char；isHigh/Low/Surrogate/Pair 区间；toCodePoint('\ud83d','\ude00')==0x1F600；负例：codePointAt(-1/越界)→StringIndexOutOfBoundsException、codePointCount 越界→IndexOutOfBoundsException、toChars(-1/0x110000)→IllegalArgumentException（消息文本两侧不同仅锁类型）。
- **Unicode 版本门（MODERN 分支）**：U+1FAE0（Emoji 15.0，isDefined/getType=OTHER_SYMBOL/UnicodeBlock SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A）、U+1E030（Unicode 15.0 modifier letter）、U+1AFF0（15.1）、U+2FFC（15.1）——JDK-8（Unicode 6.2 表）未定义，JDK-27 已定义，同文件分支各自锁定。
- **UnicodeBlock 映射**：A/Z→BASIC_LATIN、'中'→CJK_UNIFIED_IDEOGRAPHS、'é'→LATIN_1_SUPPLEMENT、0x1F600→EMOTICONS、'\ud800'→HIGH_SURROGATES、'Ａ'→HALFWIDTH_AND_FULLWIDTH_FORMS、'α'→GREEK。
- **标识符矩阵**：a-z/A-Z 四谓词（Java+Unicode start/part）、数字只 part 不 start、'_'/'$' Java 专用（**'$' 不属于 Unicode 标识符**）、'#'/' '/'-' 排除；isIdentifierIgnorable 区间 0x00-0x08/0x0E-0x1B/0x7F-0x9F true、0x09-0x0D false。
- **杂项**：valueOf 0-127 缓存恒等（0/1/'0'/'9'/'A'/'Z'/'a'/'z'/0x7F）与 >127 值等价；compare 精确差值（'A' vs 'a' == -32）；hashCode==char 值；reverseBytes 矩阵 + 对合性；MIN_VALUE=='\u0000'/MAX_VALUE=='\uffff'；charCount('A')==1。

### 使用方式
`digit(ch,radix)`↔`forDigit(d,radix)`（进制解析/格式化闭环）→ 分类谓词/getType/UnicodeBlock → `toChars`/`toCodePoint`（增广字符往返）→ isJavaIdentifierXxx（词法扫描）。

### 参考来源
- Oracle javadoc（Character：digit/forDigit 语义与 -1/'\0' 约定、isWhitespace 与 isSpaceChar 的无断行空格排除、isIdentifierIgnorable 区间、代理算术公式）。
- Unicode 版本：JDK-8=Unicode 6.2，JDK-27=Unicode 17（U+1FAE0 Emoji 15.0、U+1E030 15.0、U+1AFF0/U+2FFC 15.1，JDK-8 均未收录）。
- 双侧实测（Temurin 8u372 / 27.0.1）：所有 MODERN 分支值与异常类型为运行值。

### 测试用例设计 + 易错规范细节
- 新增方法：`testDigitMatrix`（184 断言）、`testNumericValueMatrix`（75）、`testCategoryMatrix`（79）、`testSpaceMatrix`（22）、`testISOControlMatrix`（67）、`testCaseMatrix`（90）、`testSurrogateCodePointMatrix`（39 + 5 负例）、`testUnicodeVersionGates`（6）、`testUnicodeBlockMatrix`（8）、`testIdentifierMatrix`（97）、`testMiscMatrix`（40）。
- 易错点：JDK-8 源码中增补字符必须写 "\ud834\udd1e" 代理对转义（不能直接贴字面量）；`'\u000D'` 字面量非法（unicode 转义先于词法，CR 会断行——用 `(char) 0x000D`）；getType 常量是 Java 自己的排序（SURROGATE=19、OTHER_SYMBOL=28），必须用命名常量断言；U+FFFE 非字符 isDefined==false；异常消息 8 vs 27 不同（helpful messages），只锁类型。
