# java.lang.annotation.Retention — 设计方案

## 1. API 功能
元注解，控制注解的保留策略：
- 唯一成员 `RetentionPolicy value()`（无默认值，必须显式指定）
- 自身 `@Retention(RUNTIME)`、`@Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Retention(RetentionPolicy.RUNTIME)   // 反射可见
@interface Cacheable { }
@Retention(RetentionPolicy.SOURCE)    // 编译后丢弃（如 @Override）
@interface Generated { }
```
RUNTIME 注解经 `getAnnotation/getAnnotations` 读取；SOURCE/CLASS 不可见。

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/Retention.java`：声明 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`。
- Spring 全家桶注解（`@Component` 等）均显式 `@Retention(RetentionPolicy.RUNTIME)` 以供 `AnnotationUtils` 反射扫描。
- JUnit 5 `@Test`（`junit-jupiter-api` `org.junit.jupiter.api.Test`）为 RUNTIME 保留供引擎发现。

## 4. 测试用例设计 + 自动化实现
白盒：是注解、自注解 RUNTIME、@Target(ANNOTATION_TYPE)、单成员 `value()` 无默认。
黑盒（同一测试类上并排三种策略，编译期由 javac 决定保留）：
- `@Retention` 元注解在 RUNTIME 注解上可读且值正确；Retention 自身 RUNTIME；
- 同一被注解类上 SOURCE/CLASS 两个用法运行期不可见、RUNTIME 可见、`getAnnotations()` 只剩 2 个；
- 成员值经保留策略往返；未标注 `@Retention` 的注解默认 CLASS（反射不可见）。
对应 `RetentionTest.java` 的 `testBlackBox()`。
