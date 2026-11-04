# java.lang.reflect.RecordComponent — 测试设计（JDK-27）

## 1. API 功能
`RecordComponent` 表示 record 的一个组件（经 `Class.getRecordComponents()` 获得，按声明序）：
- 元数据：`getName/getType/getGenericType/getGenericSignature/getDeclaringRecord`。
- 访问器：`getAccessor()`（返回该组件的访问器 `Method`，可直接 invoke）。
- 注解：实现 `AnnotatedElement`（`getAnnotation/getAnnotations/getDeclaredAnnotations/isAnnotationPresent`）。
- 泛型注解：`getAnnotatedType`；展示：`toString`。

## 2. Java 用户如何使用
`record` 类 → `getRecordComponents()` 按序遍历 → `getName/getGenericType` 生成序列化映射/构造器绑定 → `getAccessor().invoke(instance)` 取值。注解按 `@Target` 分发：`RECORD_COMPONENT` 目标注解留在组件上，`METHOD/FIELD/PARAMETER` 目标注解传播到访问器/字段/构造参数。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/records/`（`RecordComponentTest.java` 等）：组件顺序、注解传播、访问器反射调用。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/RecordComponent.java`。
- Jackson `RecordNaming`/jackson-databind record 支持：`getRecordComponents` + 访问器驱动 JSON 装配（同构场景 1/3）。
- Spring `BeanUtils`/`KotlinReflectionParameterNameDiscoverer` 邻接实现按组件创建绑定（场景 3）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RecordComponentTest.testBlackBox`，固定 record `Point/Labeled/Box<T>/Empty` 与 `@Comp(RECORD_COMPONENT)` 注解）：
1. 声明顺序 + 访问器活调用（x=3/y=4、返回类型/声明类/零参数、toString、非泛型组件 `getGenericSignature()==null`）。
2. 注解保留 vs 传播：`@Comp` 留组件、`@Deprecated` 传播到访问器且 `@Comp` 不传播、未注组件对照。
3. 泛型 record：`T`/`TT;` 签名、擦除 `Object.class`、`List<String>` PT 精确 typeName、`getAnnotatedType` 保类型变量、访问器对实例往返。
4. 空 record 零组件；普通类 `isRecord` false 且 `getRecordComponents()==null`。
5. 重复查询一致性。
白盒保留原有组件结构断言。产出：`RecordComponentTest.java`（36 断言通过）。

## 深化批次（2026-11-05）
新增 Part 3 八个 test 方法（行为经探针在 JDK 27.0.1 实测锁定），新增 fixture：`@Use(TYPE_USE)`、enum/interface/abstract/@interface 靶类、`Implicit`/`Explicit`/`Canonical`/`Mixed`/`Typed` record：
1. `testExplicitAccessor`：显式声明访问器的 record，getAccessor().equals(显式方法) 且 **invoke 走显式方法体（21→42）**；无显式访问器时 getAccessor 命中隐式访问器（名字精确、equals 记录类上的 Method、invoke round-trip）。
2. `testAccessorFlagSurface`：隐式访问器 accessFlags()=={PUBLIC}（public 且**非 final**）、0 参 int 返回。
3. `testCanonicalCtor`：长式 canonical 构造器下组件列表不变（长度/名字）、不再合成额外构造器（declaredConstructors==1）、newInstance 实例可正常取值。
4. `testToStringShape`：toString 精确全等——"int v"、"java.lang.String y"、"java.util.List items"、"java.lang.Object payload"（用擦除后类型的 getName）。
5. `testTypeUseAnnotation`：TYPE_USE 注解只在 getAnnotatedType() 可见（isAnnotationPresent true、值 7 往返），不进入组件声明注解（getAnnotations 空、isAnnotationPresent false）——与 RECORD_COMPONENT 目标注解的分界。
6. `testMixedOrder`：int+String 混合 record 声明序精确、非泛型组件 getGenericType()==getType()、getGenericSignature()==null、双访问器 invoke 往返、getDeclaringRecord。
7. `testNonRecordVariety`：enum/interface/abstract class/@interface/primitive/数组/普通类/String 全部 isRecord false 且 getRecordComponents()==null；record 与空 record 的 superclass==java.lang.Record。
8. `testParameterizedDeep`：List<String> 组件 ParameterizedType 深断言（rawType==List.class、actualTypeArguments[0]==String.class、ownerType null、erased type List）；T 组件是 TypeVariable（name "T"、**getGenericDeclaration()==record 类本身**、隐式 bound Object）。
产出：`RecordComponentTest.java` 94 断言通过（旧 36）。

### 易错规范细节
- `@Deprecated` 不含 RECORD_COMPONENT 目标：不会留在组件声明上，只经 FIELD/METHOD 传播路径出现在访问器/字段——组件 getAnnotations() 计数为 0。
- toString 用**擦除后类型**（T→java.lang.Object），泛型形态要走 getGenericType()/getGenericSignature()（`Ljava/util/List<Ljava/lang/String;>;`）。
- getDeclaringRecord 才是 API 名（无 getDeclaringClass）；RecordComponent 每次查询返回新实例，一致性靠字段级 equals（getName/getType/getAccessor）而非对象同一。
