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

## 契约深化（2026-11）
### API 功能
- @Documented 不改变保留期：`@Documented` 但无 @Retention（缺省 CLASS）的注解用法对反射不可见——Documented 与 Retention 正交。
- @Documented 可标注在任意 @Target 的注解上（方法目标实证 + 成员值回读）。
- Documented 自身：恰 3 个元注解（@Documented/@Retention(RUNTIME)/@Target(ANNOTATION_TYPE)）、非 @Inherited、非 @Repeatable、toString 全等 "@java.lang.annotation.Documented()"。

### 使用方式
自造 @DocMethod（METHOD 目标）与 @DocClassRet（无 @Retention）正负对照。

### 参考来源
Oracle javadoc java.lang.annotation.Documented。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testDeepenedSemantics`：方法目标回读、CLASS 缺省不可见三断言（getAnnotation/isAnnotationPresent/getAnnotations）、toString 全等、三个元注解计数、非 Inherited/Repeatable。
- 易错：Documented 只影响 javadoc 工具输出，不授予运行期可见性；测试它"无效"一面必须用缺省保留期的对照注解。
