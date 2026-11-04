# java.util.function.Consumer 设计案

## 1. API 功能
单参数消费者。抽象方法 `void accept(T)`；默认 `andThen` 顺序串联多个消费者。

## 2. Java 用户如何使用
用 lambda/方法引用消费元素；`c1.andThen(c2)` 先执行 c1 再 c2。

## 3. 开源代码实际用法调研
JDK 内部：`java.lang.Iterable.forEach(Consumer)` 遍历；`java.util.stream.Stream.forEach(Consumer)`；`java.util.Optional.ifPresent(Consumer)`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + `@FunctionalInterface` 注解 + 反射核对抽象方法与默认/静态方法签名。
2. 黑盒：lambda/方法引用实现 SAM 并调用断言；组合默认方法（and/or/negate/andThen/compose/identity/minBy/maxBy 等）走真实组合链路。

实现：`ConsumerTest.java`（`testWhiteBox` + `testBlackBox`）。
