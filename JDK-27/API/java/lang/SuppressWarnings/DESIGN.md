# SuppressWarnings — 设计方案

## 1. API 功能
`java.lang.SuppressWarnings`（注解）抑制编译器警告：`@Retention(SOURCE)`、`@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, MODULE})`，成员 `String[] value()` 列出警告类别（如 "unchecked"、"deprecation"）。

## 2. Java 用户如何使用
- 在无法避免警告的代码处加 `@SuppressWarnings("unchecked")`，告知编译器忽略该类警告。
```java
@SuppressWarnings("unchecked")
List<String> xs = (List) raw;
```

## 3. 开源代码实际用法调研
- JDK 及框架在泛型桥接/反序列化处大量使用；`test/jdk/java/lang/annotation` 验证 SOURCE 保留与 value 类型。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(SOURCE)`、`@Target` 含 TYPE。
黑盒：反射验证 `value()` 返回 `String[]`。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。
