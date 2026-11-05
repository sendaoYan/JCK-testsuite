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

## 契约深化（2026-11）

本轮追加 8 个 test 方法（断言 44 → 145）：自定义实现契约 + 五实现多态矩阵 + 默认流方法路由实证 +
静态 compare 精确矩阵 + isEmpty/getChars 默认方法矩阵。

### 新增维度

- **自定义实现契约（CS）**：四方法精确 + 链式 subSequence 相对索引 + subSequence 结果仍是
  CharSequence（可用 isEmpty 等默认方法）。
- **五实现多态矩阵**：String/StringBuilder/StringBuffer/CharBuffer/CS 逐实现断
  length/charAt/toString/subSequence(1,4)/charAt 循环==toString/`isEmpty() == (length()==0)` 不变量。
- **默认流方法路由实证（CountingCS）**：`chars().toArray()` 终结后 charAt 调用次数 == length()；
  代理对上 chars() 给 UTF-16 单元、codePoints() 解码配对且 charAt 次数 == length()。
  （`count()` 在 JDK-27 可短路优化不经 charAt，故路由断言只用 toArray。）
- **越界契约**：CS/SB/SBF 的 charAt(-1/len) 与 subSequence(-1,2)/(1,4)/(2,1) →
  IndexOutOfBoundsException（String 具体为 SIOOBE）。
- **CharBuffer 视图**：charAt 不动 position、length 计 UTF-16 单元、空缓冲 isEmpty。
- **静态 compare 精确矩阵**：字符差 -2、长度差 ±1、跨类型（SB vs String、String vs SBF、
  自定义互比）、反对称精确值、大小写差 `'H'-'h'`、第二参数 null → NPE、高 BMP 相邻 -1。
- **isEmpty 默认方法矩阵**：自定义空/非空、CharBuffer、经接口引用的 StringBuilder、
  空白串（"  "）非 empty。
- **getChars 默认方法矩阵**：全窗/部分窗/dst 偏移/空窗不拷贝、与 String.getChars 结果一致、
  CountingCS 证明默认实现逐 charAt 路由（次数 == end-start）、五种越界 IOOBE。

### 易错规范细节（新增锁定）

- JDK-27 的 CharSequence 有 isEmpty（15+）、compare（9+）、getChars（21+）默认/静态成员，
  JDK-8 均无——跨版本用例必须分支。
- 默认 chars()/codePoints()/getChars 都逐索引调 charAt：路由断言必须用 toArray/getChars 终结操作。
- compare 的差异位返回 char 差（'a'-'c'=-2），长度关系返回长度差（±1）。
