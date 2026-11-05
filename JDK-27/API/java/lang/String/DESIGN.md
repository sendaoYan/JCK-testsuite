# java.lang.String 测试方案

## 1. API 功能（按方法分组）

- 构造/工厂：`String()`、`String(byte[])`、`String(byte[], Charset)`、`String(byte[], int, int, Charset)`、`String(char[])`、`String(char[], int, int)`、`String(String)`、`String(StringBuilder)`、`String(StringBuffer)`；静态工厂 `valueOf(...)`、`copyValueOf(...)`、`format(...)`、`join(...)`。
- 核心查询：`length()`、`isEmpty()`、`isBlank()`、`charAt(int)`、`codePointAt(int)`、`codePointBefore(int)`、`codePointCount(int,int)`、`offsetByCodePoints(int,int)`。
- 比较/搜索：`equals(Object)`、`equalsIgnoreCase(String)`、`compareTo(String)`、`compareToIgnoreCase(String)`、`regionMatches(...)`、`startsWith(...)`、`endsWith(String)`、`indexOf(...)`、`lastIndexOf(...)`、`contains(CharSequence)`。
- 提取/转换：`substring(...)`、`subSequence(int,int)`、`concat(String)`、`replace(...)`、`replaceAll(String,String)`、`replaceFirst(String,String)`、`matches(String)`、`split(...)`、`toLowerCase()`/`toUpperCase()`、`trim()`、`strip()`/`stripLeading()`/`stripTrailing()`、`stripIndent()`、`repeat(int)`、`indent(int)`、`lines()`、`translateEscapes()`、`toCharArray()`、`getChars(...)`、`getBytes(...)`。
- 工具/常量 API：`intern()`、`hashCode()`、`transform(Function)`、`formatted(Object...)`、`chars()`/`codePoints()`、`describeConstable()`、`resolveConstantDesc(Lookup)`。

## 2. Java 用户如何使用

- 构造：`new String("abc")`、`new String(new char[]{'a','b'})`、`new String(bytes, UTF_8)`、`new String(sb)`（拷贝 StringBuilder 内容）、`String.valueOf(42)`、`String.join(",", ...)`、`"a-%d".formatted(1)`。
- 组合链路（真实串联）：
  1. `new String(new char[]{' ','a',' '}).strip() -> "a"`，再 `toUpperCase()`、`repeat(2)`、`contains(...)`。
  2. 解析：`"a,b,".split(",", -1)` + `String.join("-", parts)` 回拼。
  3. 文本块：`"  a\n    b".stripIndent()` + `.lines().count()` + `.indent(2)`。
  4. 转义：`"a\\nb".translateEscapes()` 得到真实换行。

## 3. 开源代码实际用法调研

来源：JDK 自带测试 `~/git/jdk25u-dev/test/jdk` 与源码 `~/git/jdk25u-dev/src`（权威、可精确引用）；Web 检索受限，仅补充 Spring 框架常规用法（非逐行核对）。

- `test/jdk/java/lang/String/Strip.java`：`"   abc   ".strip()`、`.stripLeading()`、`.stripTrailing()`，以及 strip 对 `•` 等非空白字符不误删——与黑盒 `testBlankStrip` 对齐。
- `test/jdk/java/lang/CharSequence/Comparison.java`：`CharSequence.compare(sb1, sb2)` 比较 StringBuilder/StringBuffer——与黑盒 `CharSequence.testCompare` 对齐。
- `src/java.base/share/classes/jdk/internal/util/Exceptions.java`：`StringBuilder sb = new StringBuilder(); sb.append(...)` 拼接诊断信息。
- Spring（`spring-projects/spring-framework` `org.springframework.util.StringUtils`）：`collectionToDelimitedString` 用 `StringBuilder` 循环 `append` 再 `delete` 尾部分隔符、`toString()`。

## 4. 测试用例设计 + 自动化实现

- 正常链路：构造（char[]/byte[]/StringBuilder/空）→ length/isEmpty/isBlank → charAt/codePointAt → substring/indexOf/lastIndexOf/contains/startsWith/endsWith → concat/replace/replaceAll/split/join → toUpperCase/toLowerCase/trim/strip* → equals/compareTo/hashCode → valueOf/format → repeat/indent/lines/transform/formatted/translateEscapes。
- 边界/异常：`charAt`/`substring`/`codePointAt`/`codePointBefore` 越界（`StringIndexOutOfBoundsException`）、`codePointCount` 越界（`IndexOutOfBoundsException`）、`repeat(-1)` 与非法转义（`IllegalArgumentException`）、`join(null, ...)` 与 `join(delim, null元素)`（NPE / "null"）。
- 不变量：不可变性（转换返回新串）；`intern()` 返回常量池实例；`strip` 用 `isWhitespace`（不删 NBSP/U+2007），`trim` 只删 `<=U+0020`；`indent` 归一化换行并追加尾换行；`lines()` 丢弃尾部空行。
- 自动化实现：`StringTest.testWhiteBox()`（反射：final/继承/接口/成员形态）+ `StringTest.testBlackBox()`（上述全部行为组合，含 `testBlankStrip`、`testRepeatIndentLines`、`testTransform`、`testFormattedTranslateEscapes`、`testStripIndent`、`testJoinAndCodePointBounds`）。

## 契约深化（2026-11）

本轮追加 19 个 test 方法（断言 183 → 404），与 JDK-8 侧同构的核心契约 + JDK-11+ 专属 API 精确表。

### 新增维度

- **equals/hashCode 契约**：`""`→0、`"a"`→97、`"abc"`→96354、碰撞对 `"Aa"`/`"BB"`→2112；
  手算 `31*h+c` 闭环；equals/contentEquals 对 StringBuilder 的分叉。
- **索引族负例消息逐字锁定（JDK-27 形式）**：`charAt(-1)` → `"Index -1 out of bounds for length 3"`；
  `substring(-1)` → `"Range [-1, 3) out of bounds for length 3"`（begin>end 形如 `Range [3, 2)`）；
  `codePointBefore(0)` → `"Index -1 out of bounds for length 2"`（内部 index-1）；
  `codePointCount` 越界 → `"Range [-1, 1) out of bounds for length 2"`；
  `offsetByCodePoints` 越界仍为**无消息** `IndexOutOfBoundsException`（与 8 相同）。
- **indexOf 全形态 / replace 两形态 / split limit 矩阵 / matches 点号语义**：与 8 侧同套
  （含 replace 无命中返回 this 的 == 断言、`"".split(",")`→`[""]`、`",,"`→空数组）。
- **trim 空白表**：trim 只去 `<= U+0020`（U+2003/U+00A0 不去）。
- **intern 闭环（JLS 3.10.5）**：常量折叠 ==、运行时拼接 intern() 归一、substring/builder 派生归一。
- **format 矩阵（Locale.ENGLISH）**：`%05d`/`%,d`（千分位 "1,234,567"）/`%.2f`/`%x/%X/%o`/`%b/%c/%s null`/
  `%%`/`%n`（非空）/`%-6d`/`%+d`/`%e`/`%g` 全部精确全等。
- **getBytes 矩阵**：UTF-8 `"中"` 3 字节 0xE4 开头、US-ASCII '?'=0x3F、U+1D11E 4 字节。
- **regionMatches 容错 + compareTo 精确差值 + equalsIgnoreCase 矩阵 + valueOf/join + 流内容**：同 8 侧。
- **JDK-11+ strip 精确表**：strip 依 `Character.isWhitespace`——U+2003 去两端（len 1）、U+00A0 不去
  （len 3）；无变化返回 this；stripLeading/stripTrailing 只动一端。
- **JDK-11+ repeat 契约**：`repeat(1)` 返回 **this（实测锁定）**、0→空、负数抛 IAE 精确消息
  `"count is negative: -1"`。
- **JDK-11+ lines 精确元素表**：`"a\n\nb"`→["a","","b"]（保留内部空行）；`"\n"`→恰好**一个**空行
  （非零行——易错）；`"a\nb\n"` 丢尾行；`\r`/`\r\n`/`\n` 均为终止符。
- **JDK-11+ isBlank 表**：U+00A0/U+200B 非 blank（isWhitespace false）、U+2003/U+2028 blank。

### 易错规范细节（新增锁定）

- `"\n".lines()` 是 1 行（内容为空串），不是 0 行；只有 `"".lines()` 是 0 行。
- `compareTo` 前缀关系返回长度差 ±1，不是下一字符值。
- 27 侧越界消息全部为 `Index …`/`Range […]` 形式（JDK-9+ checkIndex 统一格式），
  与 8 侧 `"String index out of range: n"` 不同，测试用 MODERN 分支双锁。

### 参考来源

- Oracle javadoc（strip/repeat/lines/isBlank since 11；repeat 负数 IAE 消息；lines 终止符语义）。
- JLS 3.10.5；OpenJDK String.java（JDK-27 源码 StringIndexOutOfBoundsException 消息格式）。
- 双侧实测（jdk8u372-b07 vs jdk-27.0.1.0.2+1）。
