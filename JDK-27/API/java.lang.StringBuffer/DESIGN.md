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
