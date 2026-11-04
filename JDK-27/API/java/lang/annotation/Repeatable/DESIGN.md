# java.lang.annotation.Repeatable — 设计方案（JDK 27）

## 1. API 功能
元注解：声明注解可重复，重复用法打包进容器注解。
- 唯一成员 `Class<? extends Annotation> value()`
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@interface Schedules { Schedule[] value(); }
@Repeatable(Schedules.class) @interface Schedule { String cron(); }
@Schedule(cron="a") @Schedule(cron="b") class Job {}
Job.class.getAnnotationsByType(Schedule.class); // 两个实例
Job.class.getAnnotation(Schedule.class);        // null
```

## 3. 开源代码实际用法调研
- JDK `Repeatable.java`；Spring `@Scheduled`、JUnit `@ExtendWith`/`@Tag`。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、RUNTIME、单成员 `value()` 无默认、@Documented/@Target(ANNOTATION_TYPE)。
黑盒：value() 指向容器；容器 `value()` 返回 `Schedule[]`；重复用法 getAnnotationsByType 返回 2 个且顺序/值正确；getAnnotation(Schedule) null、getAnnotation(容器) 非空；容器成员与结果 equals/hashCode 一致；单次用法直接可见无容器；容器非 @Repeatable。
反例（@compile/fail）：`NonRepeatableRepeated.java`、`ContainerRetentionShorter.java`、`ContainerTargetNotApplicable.java`。

## 易错规范细节（JDK 27）
- 容器 @Retention 不能短于被重复注解（声明期报错）；容器 @Target 必须覆盖被重复注解（使用期报错）。
- 反例文案 JDK 27 用“annotation interface”、JDK 8 用“annotation type”。
