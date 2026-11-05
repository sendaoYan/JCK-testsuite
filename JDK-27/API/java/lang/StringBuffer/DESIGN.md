# java.lang.StringBuffer 测试方案

## 1. API 功能（按方法分组）

- 构造：`StringBuffer()`、`StringBuffer(int)`、`StringBuffer(String)`、`StringBuffer(CharSequence)`。
- 追加：`append(...)`（boolean/char/char[]/double/float/int/long/Object/String/CharSequence/StringBuffer，均返回 `this`）、`appendCodePoint(int)`。
- 插入/删除/替换：`insert(int, ...)`、`delete(int,int)`、`deleteCharAt(int)`、`replace(int,int,String)`。
- 长度/字符：`length()`、`charAt(int)`、`setCharAt(int,char)`、`setLength(int)`、`getChars(...)`。
- 容量：`capacity()`、`ensureCapacity(int)`、`trimToSize()`。
- 其他：`reverse()`、`substring(...)`、`subSequence(...)`、`indexOf(...)`/`lastIndexOf(...)`、`chars()`/`codePoints()`、`compareTo(StringBuffer)`、`toString()`。

## 2. Java 用户如何使用

- 构造 + 链式追加（同 StringBuilder，行为一致但方法 `synchronized`）。
- 组合链路：`new StringBuffer().append('a').append("bc").append(123)` → `insert(1,"!!")` → `delete(2,4)` → `reverse().reverse()` → `setLength(4)` → `setCharAt(0,'Z')` → `toString()`。
- 容量：`new StringBuffer(8)` → `ensureCapacity(32)` → `append(...)` → `trimToSize()`。

## 3. 开源代码实际用法调研

来源：JDK 源码/测试（权威）。`StringBuffer` 因线程安全，常用于共享可变字符串与序列化场景。

- `test/jdk/java/lang/Character/CharCheck.java`、`Supplementary.java`、`UnicodeSpec.java` 等 JDK 测试用 `StringBuffer` 累积输出。
- JDK 历史实现中 `StringBuffer` 是 `StringBuilder` 的同步版本，二者公开 API 几乎一一对应（可反射比对）。
- Spring 等现代代码更偏好局部 `StringBuilder`；`StringBuffer` 保留给需要线程安全的共享场景。

## 4. 测试用例设计 + 自动化实现

- 正常链路：构造 → append 链式 → insert/delete/deleteCharAt/replace → reverse → capacity/ensureCapacity/setLength → charAt/setCharAt → substring/toString → indexOf(fromIndex)/compareTo/chars()。
- 边界/异常：`setLength` 补 NUL、`insert` 越界、`charAt(-1)`。
- 不变量：变更方法返回 `this`；默认容量 16；`trimToSize` 后 capacity==length；`compareTo` 字典序。
- 线程安全契约：`StringBuffer` 公共变更方法（`append`/`insert`/`reverse`/`length`/`toString`）**为 synchronized**（反射断言）；与 `StringBuilder` 行为一致但后者无同步（`testBehaviorEquivalence` 用相同操作序列对比二者结果相同）。
- 自动化实现：`StringBufferTest.testWhiteBox()`（反射结构 + 同步成员形态）+ `testBlackBox()`（`testConstructors`、`testAppend`、`testAppendCodePoint`、`testLengthChars`、`testInsertDelete`、`testReverseSubstringIndex`、`testCapacity`、`testCompareTo`、`testIndexOfFromIndex`、`testInsertOverloads`、`testStreams`、`testSynchronized`、`testBehaviorEquivalence`）。

## 契约深化（2026-11）

### API 功能（本轮新增覆盖）
- 与 StringBuilder 同套矩阵（独立成文不共用 harness）：append 全家精确值（boolean/char/int/long/float/double 边界值/String/char[] 全量与区间/CharSequence 四种来源 + 切片/`append(StringBuffer)`/自追加/appendCodePoint 0/0xFFFF/0x10FFFF/0x1D11E）；insert 全家（合法位 + 返回 this + null 三形态 + null 切片）；delete/deleteCharAt/replace 矩阵；setCharAt/setLength（补 NUL/丢尾/append 落点）；reverse 矩阵（代理对成对整体反转）；codePoint 家族代理对全边界；substring/subSequence（返回 String）；indexOf/lastIndexOf 边界（空串/clamp/负 from/代理 needle）；toString 快照独立 + 每次新实例；equals/hashCode 未覆写；CharSequence 多态视图；capacity 契约（16/len+16/0/ensureCapacity 不减/trimToSize==length）。
- `append((StringBuilder)null)`（经 CharSequence 重载）→ `"null"`；**JDK 21+ `repeat(int,int)`/`repeat(CharSequence,int)`**（本侧独有，负 count → `IllegalArgumentException` 消息 `count is negative: -1`）。

### 使用方式
- 同前；新增 `expectExact`（异常类名+消息逐字）与 `MODERN` 常量编码两侧差异（本侧 MODERN==true）。

### 参考来源
- Oracle javadoc StringBuffer（JDK 27 版，线程安全契约 + repeat）；JDK-8 与 JDK-27 双侧实测锁定异常类与消息。

### 测试用例设计 + 易错规范细节（本轮锁定）
- **StringBuffer 全套矩阵行为与 StringBuilder 完全一致**（同一 AbstractStringBuilder 内核，仅加 synchronized）：所有越界异常类与消息两侧逐字同 StringBuilder（含 `append(char[],off,len)` JDK-27 纯 IOOBE Range 消息、`insert(off,char)` JDK-27 SIOOBE、`insert(off,csq,s,e)` JDK-27 SIOOBE Range）。
- **`setLength(-1)` 消息 `String index out of range: -1` 两侧逐字相同**；`offsetByCodePoints` 越界两侧均为纯 IOOBE + null 消息。
- **charAt/setCharAt/deleteCharAt/codePointAt/codePointBefore/substring 越界消息**：JDK-27 `Index n out of bounds for length len` / `Range [...]` 逐字锁定。
- **toString 每次返回新 String 实例**（toStringCache 缓存的是值不是对象；append 后缓存失效）。
- **自追加**：`new StringBuffer("ab").append(自身)` → `"abab"`。
- **线程安全只做结构性断言**（方法 synchronized 修饰符），不做并发行为断言。
- **`new StringBuffer(-1)`**：两侧 NegativeArraySizeException（JDK-27 消息 `-1`）。
- **compareTo（15+）与 repeat（21+）为现代侧独有 API**，JDK-8 侧 javap 确认后不测。
