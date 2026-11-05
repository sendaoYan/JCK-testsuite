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
