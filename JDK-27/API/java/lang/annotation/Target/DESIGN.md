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

## 契约深化（2026-11）
### API 功能
- @Target({}) 声明可读回 length 0；全目标注解为 **27 侧 12 种**（含 MODULE/RECORD_COMPONENT，逐一 contains 实测锁）；七种元素运行期可见；TYPE_USE 类型位置 vs 声明视图分离；value() 防御性拷贝；值语义 equals/hashCode；非 Inherited/Repeatable。

### 使用方式
@NothingAllowed/@AllTargets(12)/@TypeUseOnly + GenericCarrier fixtures。

### 参考来源
JLS 9.6.4.1；javap（jdk-27.0.1）。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testDeepenedTargets`；27 侧额外断言全目标声明恰 12 项且含 MODULE/RECORD_COMPONENT（与 8 侧 10 项为版本差异）。
- 易错：MODULE 目标只能用于 module-info，类内无法自造用法，仅经 @AllTargets 声明回读覆盖。
