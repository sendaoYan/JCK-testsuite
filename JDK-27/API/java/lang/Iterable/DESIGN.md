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
