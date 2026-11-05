# java.lang.annotation.Inherited — 设计方案（JDK 27）

## 1. API 功能
标记元注解：标注的注解在类继承时被子类查询到。
- 仅作用于类继承；接口实现、方法覆盖不继承。
- 自身 `@Documented @Retention(RUNTIME) @Target(ANNOTATION_TYPE)`

## 2. Java 用户如何使用
```java
@Inherited @Retention(RUNTIME) @interface Tx {}
@Tx class ServiceBase {}
class ServiceSub extends ServiceBase {}
ServiceSub.class.isAnnotationPresent(Tx.class); // true
ServiceSub.class.getDeclaredAnnotation(Tx.class); // null
```

## 3. 开源代码实际用法调研
- JDK `Inherited.java`：“only applies to class inheritance”。
- Spring `@Component` 派生、`@Transactional`。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、零成员、@Documented/@Retention(RUNTIME)/@Target(ANNOTATION_TYPE)。
黑盒：四级继承链（Base/Child/GrandChild/覆写子类）；子类 getAnnotation/isAnnotationPresent 见父类、成员值一致、孙类同样、子类自有用法遮蔽父类；对照无 @Inherited 不继承；getDeclaredAnnotation/getDeclaredAnnotations 不走继承；接口实现不继承；**方法覆盖不继承**。

## 易错规范细节（JDK 27）
- @Inherited 只影响类继承；接口实现、方法/字段/构造器无继承效果。

## 契约深化（2026-11）
### API 功能
- 子接口不继承父接口注解；抽象父类→具体子类继承；getAnnotationsByType 遵守 @Inherited；Inherited 元注解画像（非自标/Documented/非 Repeatable/3 元注解）。

### 使用方式
IfaceSub/AbsBase/Conc fixtures 正负对照。

### 参考来源
@Inherited javadoc；javap（jdk-27.0.1）。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testExtendedSemantics`，与 JDK-8 同构，双侧行为一致（无版本差异）。
- 易错：@Inherited 只作用于类继承链；接口任何方向、方法覆写都不传播。
