# java.lang.reflect.TypeVariable — 测试设计（JDK-8）

## 1. API 功能
`TypeVariable<D extends GenericDeclaration>` 表示泛型声明（类/接口/方法/构造器）的类型变量：
- `getName()` 变量名；`getBounds()` 界列表（隐式补 `Object`）；`getGenericDeclaration()` 声明者；`getAnnotatedBounds()`（JDK 8 起）带注解的界镜像。
- 继承 `Type` 与 `AnnotatedElement`；无公开构造，只能经 `getTypeParameters()/getGenericParameterTypes()` 等取得。

## 2. Java 用户如何使用
泛型框架（Spring `GenericTypeResolver`、Gson `TypeToken`、Jackson `TypeReference`）经 `getTypeParameters()[i]` 取变量名与界做依赖注入解析/反序列化目标类型推断；注解处理器读 `getAnnotatedBounds()` 取 `TYPE_USE` 注解（如 `@Nullable` 界注解）。

## 3. 参考来源
- Oracle javadoc JDK 8 `java.lang.reflect.TypeVariable`（bounds 顺序=声明顺序、无界隐含 Object）。
- JDK 源码 `sun/reflect/generics/reflectiveObjects/TypeVariableImpl`（equals 按声明者+变量标识）。
- JLS 8 §4.4（类型变量、多界擦除取第一界）；JSR 308（`TYPE_USE`/`TYPE_PARAMETER` 注解位置语义）。
- javap 双侧枚举：4 个抽象方法双侧一致。

## 4. 测试用例设计 + 易错规范细节
黑盒场景（自造 `G<T extends Number, U extends List<String>>`、`Sample<T extends Number & Comparable<T>>`、`H<T>` 带 `<E extends CharSequence>` 方法、泛型构造器、`@Mark` RUNTIME 注解）：
| # | 维度 | 断言要点 |
|---|------|----------|
| 1 | 单界 | T bounds==[Number.class]；U 界是 PT `List<String>`（raw+arg 递归）——易错：PT 界不是 Class |
| 2 | 多界顺序 | `<T extends Number & Comparable<T>>` bounds==[Number, Comparable<T>-PT]，第二界自引用含变量自身 |
| 3 | 声明者 | 类级 ==G.class；方法级 ==那个 Method；泛型构造器（`<E> G(E)`）getGenericDeclaration()==那个 Constructor——易错：构造器也可声明自己的类型参数 |
| 4 | AnnotatedBounds | `T extends @Mark Number` 界上 RUNTIME 注解 isAnnotationPresent 可见；`<@Mark T extends Number>` 参数位注解**不**落在界上（易错）；双界各自带注解独立 |
| 5 | equals/hashCode | 同声明同一变量两次获取 equals 且 hashCode 相等；类 TV 与同名方法 TV 不等（同名不同声明）；toString==typeName==name |
| 6 | 层级 | extends Type 与 AnnotatedElement；无注解 TV getAnnotations() 空、getAnnotation 缺席 null |
产出：`TypeVariableTest.java`（70 断言通过）。
