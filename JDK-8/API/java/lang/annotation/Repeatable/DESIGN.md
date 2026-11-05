# java.lang.annotation.Repeatable — 设计方案（JDK 8）

## 1. API 功能
元注解：声明注解可重复使用，重复用法由编译器打包进“容器注解”。
- 唯一成员 `Class<? extends Annotation> value()`（指向容器，容器必须有 `T[] value()`）
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@interface Schedules { Schedule[] value(); }
@Repeatable(Schedules.class) @interface Schedule { String cron(); }
@Schedule(cron="a") @Schedule(cron="b") class Job {}
Job.class.getAnnotationsByType(Schedule.class); // 两个实例
Job.class.getAnnotation(Schedule.class);        // null（容器形式存储）
```

## 3. 开源代码实际用法调研
- JDK `Repeatable.java`：容器形制（`T[] value()`、保留/目标需覆盖）。
- Spring `@Scheduled`/`@PropertySource`、JUnit `@ExtendWith`/`@Tag`。
- JDK `test/jdk/java/lang/annotation/` 重复注解读写用例。

## 4. 测试用例设计 + 自动化实现
- 结构：注解类型、RUNTIME、单成员 `value()` 无默认、@Documented/@Target(ANNOTATION_TYPE)。
- `value()` 精确指向容器；容器 `value()` 返回 `Schedule[]`。
- 重复用法：`getAnnotationsByType` 返回 2 个且成员值/源顺序正确；`getAnnotation(Schedule)` 为 null、`getAnnotation(Schedules)` 非空。
- 容器成员与 getAnnotationsByType 结果 equals/hashCode 一致。
- 单次用法直接可见、无容器注解；容器本身非 @Repeatable。
- **反例（@compile/fail）**：`NonRepeatableRepeated.java`、`ContainerRetentionShorter.java`、`ContainerTargetNotApplicable.java`。

## 易错规范细节（JDK 8）
- 容器 `@Retention` 不能短于被重复注解（声明期报错）；容器 `@Target` 必须覆盖被重复注解（使用期报错）。
- 反例错误文案 JDK 8 用“annotation type”、JDK 27 用“annotation interface”，双侧各自 @compile/fail。

## 契约深化（2026-11）
### API 功能
- 三次重复：getAnnotationsByType 按源序展平 3 实例；**声明视图只含容器**（getDeclaredAnnotations 长度 1 且类型为容器）；getDeclaredAnnotation(单注解)==null / (容器)!=null；按容器类型 byType 查询返回单容器。
- 容器 value() 防御性拷贝（改返回数组后原值不变）。
- Repeatable 元注解自身：非自标（不可重复）、value() 签名全等 "public abstract java.lang.Class java.lang.annotation.Repeatable.value()"；容器 equals/hashCode 值语义。
- 容器 value 必须数组类型/保留期不短于成员注解等编译期约束由既有 @compile/fail 辅件覆盖（ContainerRetentionShorter/ContainerTargetNotApplicable/NonRepeatableRepeated），不重做。

### 使用方式
@Triple fixture + getDeclaredAnnotation 区分声明视图与展平视图。

### 参考来源
JLS 9.6.3（repeatable annotations）；AnnotatedElement javadoc。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testDeepenedRepetition`。
- 易错：getAnnotation/getDeclaredAnnotation 走"原始"路径（只见容器），getAnnotationsByType 走"展平"路径——同一元素两族 API 结果不同是核心契约不是缺陷。
