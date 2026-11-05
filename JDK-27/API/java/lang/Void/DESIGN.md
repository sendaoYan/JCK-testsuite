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

## 契约深化（2026-11）

- **API 功能补充**：结构契约（final 且**非 abstract**、唯一私有构造器、Object 超类、非数组、
  非 Serializable、包名 java.lang、bootstrap 加载）；`Void.TYPE.getSimpleName()=="void"`、
  `toString()=="void"`、`getPackage()==null`。
- **使用方式补充**：`Runnable.run()` 返回类型 `== void.class == Void.TYPE`；本类自有
  `static void noop()` 的反射返回类型断言；`Class.forName("void")` 抛 ClassNotFoundException。
- **参考来源**：实测 jdk-27.0.1.0.2 与 Temurin 8u372 双侧一致；OpenJDK Void.java 源码
  （final class + private 构造器）；任务简报原述"abstract"经实测否定。
- **新增用例（断言 9→31）**：`testStructureDeep`（17 条结构断言）、`testVoidReflection`
  （Runnable.run/noop 返回类型、forName("void") 负例、TYPE 单例引用稳定）。
- **易错细节**：`Void` 不是 abstract（实测纠正）；`Void.TYPE.getPackage()==null` 而
  `Void.class.getPackage().getName()=="java.lang"`；`isInstance` 对任何实例恒 false。
