# Iterable — 设计方案

## 1. API 功能
`java.lang.Iterable`（接口）是增强 for 循环的抽象，声明 `Iterator<T> iterator()`，并提供 default 方法 `forEach(Consumer)`（Java 8+）与 `spliterator()`。

## 2. Java 用户如何使用
- 实现 `iterator()` 后即可用 for-each；集合框架（List/Set 等）均已实现。
```java
for (String s : (Iterable<String>) Arrays.asList("a","b")) { ... }
```

## 3. 开源代码实际用法调研
- `Collection` 继承 `Iterable`；`test/jdk/java/util/Iterable` 与 `ArrayList.forEach` 验证遍历顺序与 default 方法。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Iterable")` 验证为接口，`iterator()` 返回 `Iterator`，`forEach/spliterator` 为 default 方法。
黑盒：`Arrays.asList` 验证遍历顺序、耗尽后 `next()` 抛 `NoSuchElementException`、`forEach` 按序访问、`spliterator` 估量与全量遍历。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（新增维度）
恰 1 抽象方法 iterator（3 公共方法总量）；forEach/spliterator 为 default 且默认实现基于
iterator()；类型参数名为 T。

### 2. 使用方式（新增）
自造 Box 不覆写 forEach/spliterator → 默认实现路径实证：恰好 n 次、按迭代序、Consumer 异常
**同一实例**透传且遍历即时中断（b 之后不访问）；forEach(null) → NPE（默认 requireNonNull 路径，
**无消息**，27 侧同 8 侧）；iterator() 返回 null → 增强 for NPE；不可变迭代器 remove →
UnsupportedOperationException 且迭代器仍可用；可变迭代器 remove 语义（next 前 ISE、删最近
返回）；双迭代器独立游标；空Iterable 四路径零调用；默认 spliterator 未知尺寸
（estimateSize==Long.MAX_VALUE、getExactSizeIfKnown==-1、tryAdvance 顺序、forEachRemaining）；
协变消费 Iterable<? extends CharSequence>。

### 3. 参考来源
Oracle Java SE javadoc Iterable（forEach 顺序/异常透传/NPE 契约）；JDK-27 java.base
Iterable.java 默认实现（requireNonNull + for-each）。

### 4. 测试用例设计（新增方法）
testWhiteBoxCounts（方法计数/类型参数）；testForEachEquivalence（for-each==iterator、数组与
String 非 Iterable）；testForEachDefaultSemantics（n 次计数/顺序/异常同实例中断）；
testForEachNullAction（NPE 无消息）；testNullIteratorForEnhancedFor；testImmutableIteratorRemove；
testMutableIteratorRemove；testIndependentIteratorCursors；testEmptyIterable；
testIterableIsRepeatable；testSpliteratorOnCustom；testCovariantConsumption。

易错：forEach(null) 的 NPE getMessage()==null（非 "null" 字符串）；数组/String 非 Iterable 用
Class.isAssignableFrom 断言（instanceof 对 final String 编译报错）。
