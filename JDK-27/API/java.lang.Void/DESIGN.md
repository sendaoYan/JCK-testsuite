# Void — 设计方案

## 1. API 功能
`java.lang.Void`（final）是 `void` 基本类型的包装占位类，唯一成员 `public static final Class<Void> TYPE`，无公开构造器。

## 2. Java 用户如何使用
- 反射/泛型中表示 `void` 返回类型：`Void.TYPE`、`void.class`。
```java
Method m = ...;
if (m.getReturnType() == Void.TYPE) { /* void method */ }
```

## 3. 开源代码实际用法调研
- `java.lang.reflect.Method.getReturnType()` 对 void 方法返回 `Void.TYPE`。
- JDK `test/jdk/java/lang/Void` 验证 `Void.TYPE == void.class`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Void")` 验证 final、无公开构造器、`TYPE` 字段存在。
黑盒：`Void.TYPE == void.class`、名字、`isPrimitive`、与 `Void.class` 区分。
对应 `<Simple>Test.java` 黑盒部分。
