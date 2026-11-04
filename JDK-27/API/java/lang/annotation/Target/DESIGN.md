# java.lang.annotation.Target — 设计方案（JDK 27）

## 1. API 功能
元注解：限制注解可应用的程序元素。
- 唯一成员 `ElementType[] value()`（无默认；不写 @Target 即任意目标）
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Target(ElementType.FIELD) @interface Column { }
@Column int id; // 合法；用在类型/方法/参数则编译错误
```

## 3. 开源代码实际用法调研
- JDK `Target.java` 自注解；JPA `@Column`、Spring `@Autowired`。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、RUNTIME、单成员 `ElementType[] value()`、自身 @Target(ANNOTATION_TYPE)、@Documented。
黑盒：@Target(FIELD) 出现字段；@Target({METHOD,PARAMETER}) 出现方法与形参且成员值可读；无 @Target 可用类型；各注解 value() 精确回读；field-only 不泄漏到类。
反例（@compile/fail）：`FieldOnlyOnMethod.java`、`FieldOnlyOnParameter.java`、`FieldOnlyOnType.java`。

## 易错规范细节（JDK 27）
- 未标 @Target 默认全部位置。
- 反例文案 JDK 27 用“annotation interface”、JDK 8 用“annotation type”。
