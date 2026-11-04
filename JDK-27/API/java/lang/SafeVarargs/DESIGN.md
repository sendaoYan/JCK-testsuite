# SafeVarargs — 设计方案

## 1. API 功能
`java.lang.SafeVarargs`（注解）标注不会对可变参数数组执行不安全操作的 `final`/`static` 方法或构造器：`@Retention(RUNTIME)`、`@Target({CONSTRUCTOR, METHOD})`，用于抑制泛型 varargs 的 unchecked 警告。

## 2. Java 用户如何使用
- 在 `final`/`static` 泛型 varargs 方法上加 `@SafeVarargs`。
```java
@SafeVarargs
static <T> List<T> of(T... a) { return Arrays.asList(a); }
```

## 3. 开源代码实际用法调研
- `java.util.List.of`、`Collections` 内部工具方法使用 `@SafeVarargs`；`test/jdk/java/lang/SafeVarargs` 验证保留与目标。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(RUNTIME)`、`@Target` 含 METHOD 与 CONSTRUCTOR。
黑盒：自定义 `@SafeVarargs static <T> List<T> asList(T...)` 正常调用返回正确大小。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。
