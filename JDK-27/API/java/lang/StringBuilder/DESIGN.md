# java.lang.StringBuilder 测试方案

## 1. API 功能（按方法分组）

- 构造：`StringBuilder()`、`StringBuilder(int capacity)`、`StringBuilder(String)`、`StringBuilder(CharSequence)`。
- 追加：`append(boolean/char/char[]/char[],off,len/double/float/int/long/Object/String/CharSequence/CharSequence,start,end/StringBuffer)`、`appendCodePoint(int)`（均返回 `this`，链式）。
- 插入/删除/替换：`insert(int, ...)`（多种重载）、`delete(int,int)`、`deleteCharAt(int)`、`replace(int,int,String)`。
- 长度/字符：`length()`、`charAt(int)`、`setCharAt(int,char)`、`setLength(int)`、`getChars(...)`。
- 容量：`capacity()`、`ensureCapacity(int)`、`trimToSize()`。
- 其他：`reverse()`、`substring(...)`、`subSequence(...)`、`indexOf(...)`/`lastIndexOf(...)`、`chars()`/`codePoints()`、`compareTo(StringBuilder)`、`toString()`。

## 2. Java 用户如何使用

- 构造 + 链式追加：`new StringBuilder().append('a').append("bc").append(123).toString()`。
- 组合链路：
  1. `append` 多原语 → `insert(1, "!!")` → `delete(2,4)` → `reverse().reverse()` → `setLength(4)` → `setCharAt(0,'Z')` → `toString()`。
  2. 容量：`new StringBuilder(8)` → `ensureCapacity(32)` → `append(...)` → `trimToSize()`。
  3. 搜索：`new StringBuilder("banana").indexOf("an", 2)`、`lastIndexOf("an", 2)`。

## 3. 开源代码实际用法调研

来源：JDK 源码/测试（权威）+ Spring 常规用法（Web 检索补充，非逐行核对）。

- `src/java.base/share/classes/jdk/internal/util/Exceptions.java`：`StringBuilder sb = new StringBuilder();` 后循环 `sb.append(...)` 拼诊断消息。
- `src/java.base/share/classes/jdk/internal/util/ClassFileDumper.java`：`new StringBuilder(len)` 预分配容量后追加。
- `test/jdk/java/lang/Character/UnicodeBlock/CheckBlocks.java` 等大量 `test/jdk` 用例用 `StringBuilder` 拼装字符串。
- Spring `org.springframework.util.StringUtils.collectionToDelimitedString`：`StringBuilder` 循环 `append(element).append(delim)`，末尾 `delete` 多余分隔符。

## 4. 测试用例设计 + 自动化实现

- 正常链路：构造（默认/容量/String/CharSequence）→ append 链式（多原语 + char[] + CharSequence 子串 + Object）→ appendCodePoint → insert/delete/deleteCharAt/replace → reverse → capacity/ensureCapacity/setLength → charAt/setCharAt → substring/toString → indexOf(fromIndex)/compareTo/chars()/codePoints()。
- 边界/异常：`setLength` 补 NUL、`insert` 越界（`IndexOutOfBoundsException`）、`charAt(-1)`（`StringIndexOutOfBoundsException`）。
- 不变量：`append/insert/delete/reverse` 返回 `this`；默认容量 16；`trimToSize` 后 capacity==length；`compareTo` 字典序（前缀短者小）。
- 线程安全区分：`StringBuilder` 公共变更方法**无 synchronized**（反射 `Modifier.isSynchronized` 断言），与 `StringBuffer` 形成对比。
- 自动化实现：`StringBuilderTest.testWhiteBox()`（反射结构 + 非同步成员形态）+ `testBlackBox()`（`testConstructors`、`testAppend`、`testAppendCodePoint`、`testLengthChars`、`testInsertDelete`、`testReverseSubstringIndex`、`testCapacity`、`testCompareTo`、`testIndexOfFromIndex`、`testInsertOverloads`、`testStreams`、`testNotSynchronized`）。
