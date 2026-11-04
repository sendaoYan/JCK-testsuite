# java.util.function.Function 设计案

## 1. API 功能
单参数函数式接口。抽象方法 `R apply(T)`；默认 `compose`/`andThen` 组合两个 Function；静态 `identity()` 返回恒等函数。

## 2. Java 用户如何使用
用 lambda/方法引用实现 `apply`；`f.andThen(g)` 先 f 后 g、`f.compose(g)` 先 g 后 f；`Function.identity()` 做透传。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.stream.Stream.map(Function)` 变换元素；`java.util.Optional.map(Function)` 变换值；`java.util.stream.Collectors.toMap` 的 valueMapper。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + `@FunctionalInterface` 注解 + 反射核对抽象方法与默认/静态方法签名。
2. 黑盒：lambda/方法引用实现 SAM 并调用断言；组合默认方法（and/or/negate/andThen/compose/identity/minBy/maxBy 等）走真实组合链路。

实现：`FunctionTest.java`（`testWhiteBox` + `testBlackBox`）。
