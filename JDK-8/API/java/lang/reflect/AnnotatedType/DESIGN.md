# java.lang.reflect.AnnotatedType — 测试设计（JDK-8）

## 1. API 功能
`AnnotatedType` 表示可能带类型使用（TYPE_USE）注解的类型（JDK 8 接口仅有 `getType()`，注解查询继承 `AnnotatedElement`；`getAnnotatedOwnerType` 是 9+ API，本侧不测）：
- `getType()`：底层 `Type`（Class/ParameterizedType/TypeVariable/GenericArrayType 精确断言）。
- 注解查询：`getAnnotation/getAnnotations/isAnnotationPresent/getAnnotationsByType/...`。
- 取得入口：`Field.getAnnotatedType`、`Method/Constructor.getAnnotatedReturnType`、`Parameter.getAnnotatedType`、`Class.getAnnotatedSuperclass/getAnnotatedInterfaces`、`Executable.getAnnotatedReceiverType`。

## 2. Java 用户如何使用
在类型位置（字段/返回值/参数/超类/接口/接收者）读取 `TYPE_USE` 注解：Bean Validation 容器元素校验、注入类型限定、序列化器类型标记；`getAnnotatedSuperclass/getAnnotatedInterfaces` 用于类层级上的类型注解（框架代理/校验扫描超类约束）。

## 3. 参考来源
- Oracle javadoc（Java 8 `AnnotatedType`、`Class.getAnnotatedSuperclass/getAnnotatedInterfaces`、`Executable.getAnnotatedReceiverType`）：null 语义（Object 自身/接口/注解类型 → null superclass）。
- OpenJDK 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/AnnotatedType.java`、`sun/reflect/annotation/AnnotatedTypeFactory.java`。
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/annotation/typeUse/`、`~/git/jdk/test/jdk/java/lang/reflect/AnnotatedElement/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/annotation/`（类型注解边界用例）。

## 4. 测试用例设计 + 易错规范细节
fixture：`@A(TYPE_USE, TYPE)`、`Super/SubAnno extends @A Super/SubPlain/SubGen extends ArrayList<@A String>`、接口族 `Iface1/Iface2/SubIface/ImplTwo implements @A Iface1, Iface2`、注解类型 `@AnnoDecl`、`enum Color`、`Receiver<T>` 泛型类。

| test 方法 | 场景 | 关键断言 |
|---|---|---|
| testAnnotatedType | 白盒保留 | 返回类型 getType/getAnnotations |
| testSuperclass | 超类全景 | `@A Super` 注解可见；plain 超类非 null 且无注解；`Object.class`/接口/注解类型 superclass == null（8044629 修复后行为）；隐式超类 type==Object 非 null；泛型超类 instanceof APT 且实参带 @A；enum 超类是 ParameterizedType raw==Enum |
| testInterfaces | 接口全景 | 双接口声明序 [Iface1(@A), Iface2(无)]；无接口长度 0；接口的 getAnnotatedInterfaces 含父接口；注解类型隐式实现 Annotation（长度 1、getType()==Annotation.class）；Object 长度 0 |
| testQuerySurface | 注解查询面 | `@A String` 字段：七查询全命中 + 同一实例 + equals/hashCode/toString 自反 |
| testReceiverType | 接收者 | 实例方法非 null 且 getType()==声明类（8 返回 raw Class）；静态方法 null；本 JDK 上顶层式类构造器 receiver 非 null（实测记录，见差异） |
| testNullArguments | 负例+版本差异 | byType 两方法 null → NPE；`getAnnotation(null)`/`isAnnotationPresent(null)`/`getDeclaredAnnotation(null)` 在本 JDK 族**不抛**（返回 null/false，实测记录） |

易错规范细节（实测锁定）：
1. JDK-8 族 `AnnotatedType` 实现对 null 参数宽松：仅 byType 变体 NPE，单值查询静默返回 null/false——与 JDK-9+ 全 NPE 不同，双侧各自断言记录版本差异。
2. `Class.getAnnotatedSuperclass()`：只有 Object 自身、接口、注解类型、原始类型/void 返回 null；普通类（含隐式 extends Object）都非 null。
3. enum 的超类视图是泛型 `Enum<E>`（ParameterizedType），不是 raw Class。
4. 注解类型（`@interface`）的 `getAnnotatedInterfaces()` 恰含一个 `java.lang.annotation.Annotation`。
5. 实例方法接收者类型在 8 返回 raw Class；顶层式类构造器在 8 返回非 null（9+ 返回 null）——均为实测版本差异。
6. 元注解（TYPE_USE 等）只能写在代码里，jtreg 头注释禁用。

产出：`AnnotatedTypeTest.java`（66 断言通过，jtreg 全绿）。
