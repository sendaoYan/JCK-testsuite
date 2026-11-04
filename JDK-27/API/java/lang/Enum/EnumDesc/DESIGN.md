# Enum.EnumDesc — 设计方案

## 1. API 功能
`java.lang.Enum.EnumDesc<E>`（final，继承 `DynamicConstantDesc<E>`，实现 `ConstantDesc`）是枚举常量的 nominal 描述符：静态工厂 `of(ClassDesc, String constantName)`，`resolveConstantDesc(Lookup)` 解析为真实枚举常量，`toString`、继承的 `constantName()`。

## 2. Java 用户如何使用
- 动态常量/condy 场景（`ldc` 常量池、`ConstantDynamic`）中描述枚举常量；API 为反射式元编程提供枚举的符号形式。
```java
EnumDesc<TimeUnit> d = EnumDesc.of(ClassDesc.of("java.util.concurrent.TimeUnit"), "SECONDS");
TimeUnit u = d.resolveConstantDesc(MethodHandles.lookup());
```

## 3. 开源代码实际用法调研
- Spring 6 / Hibernate 7 的 methodhandle 元模型用 `EnumDesc` 表示枚举字段取值；JDK `test/jdk/java/lang/constant/EnumDescTest.java` 验证 of/resolveConstantDesc/toString/equals。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Enum$EnumDesc")` 验证 final、继承 `DynamicConstantDesc`、可赋值 `ConstantDesc`、`of` public static、`resolveConstantDesc(Lookup)` 存在。
黑盒：`of` 构造 TimeUnit.SECONDS 描述符，`constantName`/`toString`、`resolveConstantDesc` 得到真实常量；解析不存在的常量抛 `IllegalArgumentException`；值语义 equals/hashCode。
对应 `EnumDescTest.java` 白盒 + 黑盒部分。
