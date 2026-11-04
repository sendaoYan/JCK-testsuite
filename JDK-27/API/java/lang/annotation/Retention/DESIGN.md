# java.lang.annotation.Retention — 设计方案（JDK 27）

## 1. API 功能
元注解：控制保留策略。
- 唯一成员 `RetentionPolicy value()`（无默认）
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Retention(RetentionPolicy.RUNTIME) @interface Cacheable { }
@Retention(RetentionPolicy.SOURCE)  @interface Generated { }
```

## 3. 开源代码实际用法调研
- JDK `Retention.java`；Spring/Jakarta 显式 RUNTIME；Lombok SOURCE/CLASS。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、单成员 `value()` 无默认、自注解 RUNTIME、@Target(ANNOTATION_TYPE)、@Documented。
黑盒：SOURCE/CLASS/RUNTIME 三策略并排，SOURCE/CLASS 运行期不可见、RUNTIME 可见、getAnnotations 只剩 2 个；成员值往返；未标 @Retention 默认 CLASS。

## 易错规范细节（JDK 27）
- 无 @Retention 默认 CLASS；@Retention 自身必须 RUNTIME。
