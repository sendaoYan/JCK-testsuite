# java.util.function.Supplier 设计案

## 1. API 功能
零参数供应者。抽象方法 `T get()`，无默认/静态方法。

## 2. Java 用户如何使用
用 lambda/方法引用延迟产生值；`Supplier` 通常传给 `Optional.orElseGet`/工厂式代码。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.Optional.orElseGet(Supplier)` 惰性默认值；`java.util.Objects.requireNonNull(obj, Supplier)` 惰性消息。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + `@FunctionalInterface` 注解 + 反射核对抽象方法与默认/静态方法签名。
2. 黑盒：lambda/方法引用实现 SAM 并调用断言；组合默认方法（and/or/negate/andThen/compose/identity/minBy/maxBy 等）走真实组合链路。

实现：`SupplierTest.java`（`testWhiteBox` + `testBlackBox`）。
