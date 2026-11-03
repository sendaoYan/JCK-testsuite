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
