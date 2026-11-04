# java.lang.annotation.Documented — 设计方案

## 1. API 功能
标记元注解（无成员）：指示使用该注解的元素的 javadoc 中包含注解。
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@interface Visible { }
// javadoc of @Visible-using elements will show the annotation
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/Documented.java`：自注解标记注解。
- JDK `Deprecated`、`Retention`、`Target` 等自身都带 `@Documented`（源码可见）。
- Spring `@Component`、JAX-B/Jakarta 注解普遍组合 `@Documented + @Retention(RUNTIME) + @Target(...)` 三件套。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、零成员、自注解、RUNTIME 保留。
黑盒：
- 自定义 `@Doc` 上 `@Documented` 可读：annotationType/equals/toString；
- 对照组 `@NoDoc` 无 `@Documented`；
- 两个注解并用于同一类的可见性互不影响；
- JDK 自家 `Retention`/`Deprecated` 均带 `@Documented`；
- 以 `Annotation` 视角访问。
对应 `DocumentedTest.java` 的 `testBlackBox()`。
