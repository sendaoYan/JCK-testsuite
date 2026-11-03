# java.lang.annotation.Repeatable — 设计方案

## 1. API 功能
元注解：声明注解可重复使用，重复用法由编译器打包进“容器注解”：
- 唯一成员 `Class<? extends Annotation> value()`（指向容器，容器必须有 `T[] value()`）
- 自身 `@Retention(RUNTIME) @Target(ANNOTATION_TYPE) @Documented`

## 2. Java 用户如何使用
```java
@interface Schedules { Schedule[] value(); }
@Repeatable(Schedules.class) @interface Schedule { String cron(); }
@Schedule(cron="a") @Schedule(cron="b") class Job {}
Job.class.getAnnotationsByType(Schedule.class); // 两个实例
Job.class.getAnnotation(Schedule.class);        // null（容器形式存储）
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/Repeatable.java`：javadoc 规定容器形制（`T[] value()`，容器与成员保留策略需一致）。
- Spring `@Scheduled` 组合 `@Repeatable(Schedules.class)`（`spring-context` `org/springframework/scheduling/annotation/Schedules.java`）；`@PropertySource`/`@ComponentScan` 同模式。
- JDK `test/jdk/java/lang/annotation/AnnotationsInheritanceOrderRedefinitionTest.java`、`DuplicateAnnotationsTest.java` 覆盖重复注解的读写路径。
- JUnit `@ExtendWith`、`@Tag` 均为 `@Repeatable`，支持在测试类上重复声明。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、RUNTIME、单成员 `Class<? extends Annotation> value()` 无默认。
黑盒：
- `value()` 精确指向容器类型；容器 `value()` 返回 `Schedule[]`；
- 重复用法：`getAnnotationsByType` 返回 2 个且成员值/源顺序正确；`getAnnotation` 返回 null 而 `getAnnotation(容器)` 非空；
- 容器成员与 getAnnotationsByType 结果 equals/hashCode 一致；
- 单次用法直接可见、无容器注解出现在 `getAnnotations`；
- 两个实例 equals/hashCode 契约；容器本身非 @Repeatable。
对应 `RepeatableTest.java` 的 `testBlackBox()`。
