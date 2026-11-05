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

## 契约深化（2026-11）

### API 功能（本轮新增覆盖）
- append 全家精确值矩阵：boolean/char/int/long/float/double（含 `Integer.MIN_VALUE`/`Long.MIN_VALUE` 十进制全等）/String/char[] 全量与区间/CharSequence（String、StringBuilder、StringBuffer、自定义 WrapCs 四种来源 + (start,end) 切片）/`append(StringBuffer)` 跨类型/自追加（`sb.append(sb)` 内容翻倍）/`appendCodePoint(0/0xFFFF/0x10FFFF/0x1D11E)`。
- insert 全家（0/中间/length 合法位 + 每个重载返回 this + null 三形态与 null CharSequence 切片）。
- delete/deleteCharAt/replace（区间替换/增长/缩短/空串=delete/端点形态/返回 this）、setCharAt/setLength（增长补 `'\u0000'`、缩短丢尾、append 落点在 NUL 之后）。
- reverse 矩阵：ASCII/回文/空串/单字符/代理对按单字符整体成对反转（`"a𝄞b"` → `"b𝄞a"`，codePointCount 保持 3）/孤立代理作为普通单元/对合。
- codePoint 家族：codePointAt/codePointBefore/codePointCount/offsetByCodePoints 在代理对上的全部边界值。
- substring/subSequence（subSequence 返回 String 类实例、每次新 String）、indexOf/lastIndexOf（空串 0/len、fromIndex 越界 clamp、lastIndexOf 负 from、代理对 needle、大小写敏感）。
- toString 快照独立性（取串后 append/setCharAt 不影响快照、两次 toString 相等但不同实例）、`String.contentEquals`。
- equals/hashCode 未覆写（同内容两实例 equals false，toString().equals 桥接对比）、CharSequence 多态视图 + 接口 default chars()/codePoints()。
- capacity 契约：`()=16`、`(String)/(CharSequence)=str.length()+16`、`(0)` 合法、ensureCapacity 负/0/小于当前不改、大于则增长、append 后 capacity>=length 弱断言、trimToSize 后 capacity==length 且幂等。
- **JDK 21+ 特有 `repeat(int,int)`/`repeat(CharSequence,int)`**（返回 this、count 0 不追加、负 count → `IllegalArgumentException` 消息 `count is negative: -1`）。

### 使用方式
- 同前：链式 append/insert；新增通过 `expectExact`（异常类名+消息逐字断言）与 `MODERN` 常量把 JDK-8 与 JDK-27 的差异编码进同一份源文件（本侧 MODERN==true）。

### 参考来源
- Oracle javadoc StringBuilder/CharSequence/Appendable（JDK 27 版，含 repeat）；JDK-8 与 JDK-27 双侧实测（javap 枚举 + 每个负例两侧运行锁定异常类与消息文本）。

### 测试用例设计 + 易错规范细节（本轮锁定）
- **`append((CharSequence)null, s, e)` 规范特例**：null 被当 `"null"` 再按区间切片 —— `(null,1,4)→"ull"`、`(null,0,4)→"null"`、`(null,4,4)→""`。
- **`append((StringBuffer)null)` → 追加 `"null"`**；`append((char[])null)` → NPE（数组与对象语义分叉）。
- **自追加**：`new StringBuilder("ab").append(自身)` → `"abab"`（按当前字符序列内容复制，无自引用无限循环）。
- **reverse 保留代理对**：JDK-8 与 JDK-27 一致，成对作为单字符整体移动（规范行为，非交换高低代理）。
- **appendCodePoint 非法码点**：JDK-27 消息 `Not a valid Unicode code point: 0x110000`/`0xFFFFFFFF`（JDK-8 消息 null）。
- **`append(char[],off,len)` 越界**：JDK-27 抛**纯** `IndexOutOfBoundsException` 消息 `Range [a, b) out of bounds for length n`（JDK-8 是 ArrayIndexOutOfBoundsException + null 消息）。
- **`insert(off,char)` 越界**：JDK-27 SIOOBE（Range 消息）；其余 insert 重载越界也均 SIOOBE（消息 `Range [n, len) out of bounds for length n`，JDK-8 是 `String index out of range: n`）。
- **`insert(off, csq, s, e)` 越界**：JDK-27 SIOOBE Range 消息（JDK-8 是纯 IOOBE + `dstOffset 3` 消息）。
- **`setLength(-1)`/`setLength(MIN_VALUE)` 消息两侧逐字相同**（`String index out of range: -1` / `-2147483648`），无 MODERN 分支直接锁定。
- **`offsetByCodePoints` 越界**：两侧均为纯 `IndexOutOfBoundsException` 且消息 null —— 直接锁定。
- **charAt/setCharAt/deleteCharAt/codePointAt/codePointBefore 越界**：JDK-27 消息 `Index n out of bounds for length len` 或 `Range [...]`，逐字锁定。
- **substring(4)（len+1）JDK-27 消息是 `Range [4, 3) out of bounds for length 3`**（JDK-8 是 `String index out of range: -1`）。
- **`new StringBuilder(-1)`**：两侧 `NegativeArraySizeException`；JDK-27 消息 `-1`（JDK-8 null）。
- **capacity==0 构造器合法**；`trimToSize` 后 capacity==length（两侧一致已锁）；增长策略只锁 `capacity >= length` 弱不变量。
- **compareTo（JDK 15+）与 repeat（JDK 21+）为现代侧独有 API**，JDK-8 侧 javap 确认后不测。
