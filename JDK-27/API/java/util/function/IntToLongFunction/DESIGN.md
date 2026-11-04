# java.util.function.IntToLongFunction 设计案

## 1. API 功能
int 转 long。抽象 `long applyAsLong(int)`。

## 2. Java 用户如何使用
用 lambda 把 int 扩展为 long。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.stream.IntStream.mapToLong(IntToLongFunction)`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface + `@FunctionalInterface` 注解 + 反射核对抽象方法与默认/静态方法签名。
2. 黑盒：lambda/方法引用实现 SAM 并调用断言；组合默认方法（and/or/negate/andThen/compose/identity/minBy/maxBy 等）走真实组合链路。

实现：`IntToLongFunctionTest.java`（`testWhiteBox` + `testBlackBox`）。
