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
