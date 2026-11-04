# java.lang.annotation.Documented — 设计方案（JDK 27）

## 1. API 功能
标记元注解（无成员）：标注它的注解出现在被注解元素的 javadoc。
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Documented @Retention(RUNTIME) @interface Visible { }
```

## 3. 开源代码实际用法调研
- JDK `Documented.java` 自注解。
- JDK `Deprecated`/`Retention`/`Target` 带 `@Documented`。
- Spring/Jakarta 组合三件套。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、零成员、自注解 @Documented、@Retention(RUNTIME)、@Target(ANNOTATION_TYPE)。
黑盒：自定义 @Doc 上 @Documented 可读；对照组 @NoDoc 无；两个 RUNTIME 注解可见性互不影响；isAnnotationPresent(Documented) 区分；JDK `Retention`/`Deprecated` 均带 @Documented。

## 易错规范细节（JDK 27）
- `@Documented` **不**要求被标注注解 retention=RUNTIME（已验证双侧 javac 编译通过）；它只影响 javadoc 显示，运行时可见性由 @Retention 独立决定。
