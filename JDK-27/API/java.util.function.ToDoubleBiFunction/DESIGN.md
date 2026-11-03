# java.util.function.ToDoubleBiFunction 设计案

## 1. API 功能
双对象转 double。抽象 `double applyAsDouble(T,U)`。

## 2. Java 用户如何使用
用 lambda 从两个对象计算 double。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.stream.Collectors` 归约类场景。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + `@FunctionalInterface` 注解 + 反射核对抽象方法与默认/静态方法签名。
2. 黑盒：lambda/方法引用实现 SAM 并调用断言；组合默认方法（and/or/negate/andThen/compose/identity/minBy/maxBy 等）走真实组合链路。

实现：`ToDoubleBiFunctionTest.java`（`testWhiteBox` + `testBlackBox`）。
