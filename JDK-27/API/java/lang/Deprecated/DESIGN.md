# Deprecated — 设计方案

## 1. API 功能
`java.lang.Deprecated`（注解）标记已过时 API：`@Retention(RUNTIME)`、`@Target({CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE})`。Java 9+ 新增成员 `String since()` 与 `boolean forRemoval()`。

## 2. Java 用户如何使用
- 在过时方法/类型上加 `@Deprecated`，编译与 IDE 提示；Java 9+ 用 `@Deprecated(since="9", forRemoval=true)` 标注版本与计划移除。
```java
@Deprecated(since="1.2")
void legacy() {}
```

## 3. 开源代码实际用法调研
- JDK 内部旧 API（`Thread.stop` 等）大量标注 `@Deprecated`；`test/jdk/java/lang/Deprecated` 验证 RUNTIME 保留与成员。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(RUNTIME)`、`@Target` 含 METHOD/TYPE。
黑盒：自定义方法加 `@Deprecated` 后反射可见；反射探测 `since()/forRemoval()` 的返回类型（JDK 8 缺失则不判）。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。
