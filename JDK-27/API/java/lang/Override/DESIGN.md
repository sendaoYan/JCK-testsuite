# Override — 设计方案

## 1. API 功能
`java.lang.Override`（注解）标记方法覆写：`@Retention(SOURCE)`、`@Target(METHOD)`，仅编译期校验（方法确实覆写超类/接口方法），运行期不保留。

## 2. Java 用户如何使用
- 覆写方法上加 `@Override` 让编译器检查签名匹配，避免拼写错误。
```java
@Override public String toString() { return "x"; }
```

## 3. 开源代码实际用法调研
- JDK 及各类框架广泛使用 `@Override`；`test/jdk/java/lang/annotation` 验证 SOURCE 保留（运行期不可见）。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(SOURCE)`、`@Target(METHOD)`。
黑盒：子类覆写方法标注 `@Override`，反射确认运行期该注解不存在（SOURCE 保留）。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。
