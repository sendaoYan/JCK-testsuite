# java.util.function.BinaryOperator 设计案

## 1. API 功能
二元运算（BiFunction 特化，同类型 T,T->T）。静态 `minBy`/`maxBy` 用比较器取极值。

## 2. Java 用户如何使用
`Integer::sum` 等实现；`BinaryOperator.minBy/maxBy(Comparator)` 构造取小/取大算子。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.stream.Collectors.reducing(BinaryOperator)` 归约；`Stream.reduce(BinaryOperator)`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + `@FunctionalInterface` 注解 + 反射核对抽象方法与默认/静态方法签名。
2. 黑盒：lambda/方法引用实现 SAM 并调用断言；组合默认方法（and/or/negate/andThen/compose/identity/minBy/maxBy 等）走真实组合链路。

实现：`BinaryOperatorTest.java`（`testWhiteBox` + `testBlackBox`）。
