# java.lang.annotation.Documented — 设计方案（JDK 8）

## 1. API 功能
标记元注解（无成员）：标注它的注解会出现在被注解元素的 javadoc 中。
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Documented @Retention(RUNTIME) @interface Visible { }
// javadoc 会显示使用 @Visible 的元素上的该注解
```

## 3. 开源代码实际用法调研
- JDK `Documented.java`：自注解标记。
- JDK `Deprecated`/`Retention`/`Target` 均带 `@Documented`。
- Spring/Jakarta 注解普遍组合 `@Documented + @Retention(RUNTIME) + @Target(...)`。

## 4. 测试用例设计 + 自动化实现
- 结构：注解类型、零成员、自注解 @Documented、@Retention(RUNTIME)、@Target(ANNOTATION_TYPE)。
- 自定义 `@Doc` 上 @Documented 可读（annotationType/equals/toString）；对照组 `@NoDoc` 无。
- 两个 RUNTIME 注解并用的运行时可见性互不影响；`isAnnotationPresent(Documented)` 区分。
- JDK 自家 `Retention`/`Deprecated` 均带 @Documented。

## 易错规范细节（JDK 8）
- `@Documented` **不**要求被标注注解 retention=RUNTIME（已验证：`@Documented @Retention(CLASS)` 双侧 javac 均编译通过）。它只影响 javadoc 显示，不影响运行时可见性。
- 运行时可见性由 `@Retention` 独立决定。
