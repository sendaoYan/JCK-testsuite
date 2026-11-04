# java.lang.CharSequence 测试方案

## 1. API 功能（按方法分组）

- 抽象方法（实现类必须提供）：`length()`、`charAt(int)`、`subSequence(int,int)`、`toString()`。
- 默认方法：`isEmpty()`（since 15）、`chars()`（since 1.8）、`codePoints()`（since 1.8）、`getChars(int,int,char[],int)`（since 25）。
- 静态方法：`compare(CharSequence, CharSequence)`（since 11，字典序，同类型且 Comparable 时走 `compareTo`）。

## 2. Java 用户如何使用

- 以接口类型接收多种实现：`CharSequence cs = "hello";` 或自定义实现。
- 组合链路：
  1. `cs.subSequence(1,4)` + `CharSequence.compare(cs, other)`。
  2. `cs.isEmpty()` / `cs.chars().count()` / `cs.codePoints().sum()`。
  3. 默认 `getChars(0, n, dst, 0)` 拷贝（String 覆盖，自定义实现走默认逐 `charAt`）。

## 3. 开源代码实际用法调研

来源：JDK 测试（权威）。

- `test/jdk/java/lang/CharSequence/Comparison.java`：`CharSequence.compare(sb1, sb2)` 比较 `StringBuilder`/`StringBuffer`/`String` 之间的字典序；跨类型比较不要求同类。
- JDK 大量 API 以 `CharSequence` 为参数（如 `StringBuilder.append(CharSequence)`、`String.contains(CharSequence)`、`String.replace(CharSequence,...)`），使 `String`/`StringBuilder`/`StringBuffer`/`CharBuffer` 通用。

## 4. 测试用例设计 + 自动化实现

- 正常链路：以 `String` 与自定义 `CS`（不覆盖 `getChars`）为实例 → length/charAt/subSequence/toString → `isEmpty`（默认）→ `chars`/`codePoints`（默认）→ `CharSequence.compare` → 默认 `getChars`。
- 边界/异常：`charAt(-1/长度)`、`subSequence` 越界（`StringIndexOutOfBoundsException`）、`compare(null, ...)`（NPE）、`getChars` 越界（`IndexOutOfBoundsException`）。
- 不变量：`subSequence(0,length())==this`（对 String）；`compare` 满足自反/反对称（`a.compare(b) == -b.compare(a)` 符号）；`isEmpty() == (length()==0)`。
- 自动化实现：`CharSequenceTest.testWhiteBox()`（反射：接口/抽象方法/default 方法/static `compare`）+ `testBlackBox()`（`testStringAsCharSequence`、`testCharAtBounds`、`testSubSequenceBounds`、`testStreams`、`testIsEmpty`、`testCompare`、`testGetChars`）。
