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

## 契约深化（2026-11）
### API 功能
- `@Target({})` 空数组是合法声明：value().length==0 可读回（任何用法均编译错，但类型可声明可反射）。
- 全目标注解（JDK-8 十种目标全列）：type/field/constructor/ctor-param/method/method-param/annotation-type 七种元素逐一运行期可见。
- TYPE_USE-only 注解写进类型位置：落在类型参数上而**不在方法声明上**（声明视图与类型视图分离，`<@TypeUseOnly T>` + getTypeParameters()[0] 实证）。
- value() 数组防御性拷贝；Target 实例值语义 equals/hashCode；Target 非 @Inherited/非 @Repeatable。
- 错误目标为编译期错误，由目录内既有 @compile/fail 辅件覆盖（FieldOnlyOnMethod/Parameter/Type），运行期只测正空间。

### 使用方式
@NothingAllowed/@AllTargets/@TypeUseOnly + GenericCarrier fixtures。

### 参考来源
JLS 9.6.4.1（@Target 与适用位置）；JSR 308 类型位置语义。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testDeepenedTargets`。
- 易错：TYPE_USE 注解在方法泛型参数上时方法本身的 getAnnotation 是 null——必须查 getTypeParameters()；PACKAGE 目标只能标 package-info，测试类内无法自造，由 @AllTargets 声明回读覆盖。
