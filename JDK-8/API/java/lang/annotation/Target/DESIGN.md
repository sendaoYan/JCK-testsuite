# java.lang.annotation.Target — 设计方案（JDK 8）

## 1. API 功能
元注解：限制注解可应用的程序元素。
- 唯一成员 `ElementType[] value()`（无默认；不写 @Target 即任意目标）
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Target(ElementType.FIELD) @interface Column { }
@Column int id; // 合法；用在类型/方法/参数上则编译错误
m.getAnnotation(Inject.class); // 运行期读取合法目标上的用法
```

## 3. 开源代码实际用法调研
- JDK `Target.java`：自注解 `@Target(ANNOTATION_TYPE)`。
- JPA `@Column` 为 `@Target({METHOD, FIELD})`；Spring `@Autowired` 覆盖多目标。

## 4. 测试用例设计 + 自动化实现
- 结构：注解类型、RUNTIME 保留、单成员 `ElementType[] value()`、自身 @Target(ANNOTATION_TYPE)、@Documented。
- `@Target(FIELD)` 出现在字段；`@Target({METHOD,PARAMETER})` 出现在方法与形参且成员值可读。
- 无 @Target 注解可用于类型（任意目标）。
- 各注解声明的 `value()` 精确回读（长度/常量/声明顺序）；field-only 注解未泄漏到类。
- **反例（@compile/fail）**：`FieldOnlyOnMethod.java`、`FieldOnlyOnParameter.java`、`FieldOnlyOnType.java`。

## 易错规范细节（JDK 8）
- 未标 `@Target` 的注解默认可用于所有位置（TYPE/FIELD/METHOD/PARAMETER/CONSTRUCTOR/LOCAL_VARIABLE/ANNOTATION_TYPE/PACKAGE/TYPE_PARAMETER/TYPE_USE）。
- 错误文案 JDK 8 用“annotation type”、JDK 27 用“annotation interface”，双侧各自 @compile/fail。
