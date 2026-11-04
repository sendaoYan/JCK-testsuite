# FunctionalInterface — 设计方案

## 1. API 功能
`java.lang.FunctionalInterface`（注解）标记函数式接口（恰好一个抽象方法）：`@Retention(RUNTIME)`、`@Target(TYPE)`，供 lambda/方法引用作为目标类型。

## 2. Java 用户如何使用
- 声明函数式接口并加 `@FunctionalInterface`，编译器校验抽象方法数量；然后用作 lambda 目标。
```java
@FunctionalInterface
interface FI { int apply(int x); }
FI f = x -> x * 2;
```

## 3. 开源代码实际用法调研
- `java.util.function` 全部接口标注 `@FunctionalInterface`；`test/jdk/java/lang/FunctionalInterface` 验证 RUNTIME 保留与 lambda 可用性。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(RUNTIME)`、`@Target(TYPE)`。
黑盒：自定义 `@FunctionalInterface` 接口用作 lambda 目标并正确求值；反射确认接口上的注解存在。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。
