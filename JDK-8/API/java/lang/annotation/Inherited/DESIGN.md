# java.lang.annotation.Inherited — 设计方案（JDK 8）

## 1. API 功能
标记元注解（无成员）：标注它的注解在**类继承**时被子类查询到。
- 仅作用于类继承；接口实现、方法覆盖均不继承。
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
- JDK `Inherited.java` javadoc：“only applies to class inheritance”。
- Spring `@Component` 派生注解依赖 `@Inherited`；`@Transactional` 同理。

## 4. 测试用例设计 + 自动化实现
- 结构：注解类型、零成员、@Documented/@Retention(RUNTIME)/@Target(ANNOTATION_TYPE)。
- Base/Child/GrandChild/覆写子类四级继承链：子类 getAnnotation/isAnnotationPresent 见父类、成员值一致、孙类同样、子类自有用法遮蔽父类。
- 对照（无 @Inherited）不继承；getDeclaredAnnotation/getDeclaredAnnotations 不走继承。
- 接口实现类不继承；**方法覆盖不继承**（新增：基类方法 @Inherited 注解，子类 override 方法查询不到）。

## 易错规范细节（JDK 8）
- `@Inherited` 只影响类继承；对接口实现、方法/构造器/字段无继承效果。
- 子类 `getAnnotation` 返回的是继承自超类的注解实例，成员值与超类一致。

## 契约深化（2026-11）
### API 功能
- 接口两级不继承补全：**子接口不继承父接口**的 @Inherited 注解（ISub→IfaceBase 负例）。
- 抽象父类标注 → 具体子类继承（@Tag("abs") on AbsBase → Conc 回读）。
- getAnnotationsByType 与 getAnnotation 同样遵守 @Inherited（Child/Conc 实证）。
- Inherited 元注解自身画像：非自标、@Documented、非 @Repeatable、恰 3 个元注解。

### 使用方式
新增 IfaceSub/AbsBase/Conc fixtures，正负对照。

### 参考来源
@Inherited javadoc（"applies only to class inheritance"）。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testExtendedSemantics`。
- 易错：接口→接口与接口→实现类两个方向都不继承；声明视图（getDeclaredAnnotations）永远不受 @Inherited 影响。
