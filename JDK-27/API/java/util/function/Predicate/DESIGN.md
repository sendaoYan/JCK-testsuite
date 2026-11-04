# java.util.function.Predicate 设计案

## 1. API 功能
布尔谓词。抽象方法 `boolean test(T)`；默认 `and`/`or`/`negate` 组合；静态 `isEqual`/`not`。

## 2. Java 用户如何使用
用 lambda 写条件；`p.and(q)`/`p.or(q)` 逻辑组合；`p.negate()` 取反；`Predicate.isEqual(x)`/`Predicate.not(p)` 便捷构造。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.stream.Stream.filter(Predicate)`/`allMatch`/`anyMatch`；`java.util.Optional.filter(Predicate)`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + `@FunctionalInterface` 注解 + 反射核对抽象方法与默认/静态方法签名。
2. 黑盒：lambda/方法引用实现 SAM 并调用断言；组合默认方法（and/or/negate/andThen/compose/identity/minBy/maxBy 等）走真实组合链路。

实现：`PredicateTest.java`（`testWhiteBox` + `testBlackBox`）。
