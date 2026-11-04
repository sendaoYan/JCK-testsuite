# java.lang.reflect.TypeVariable — 测试设计（JDK-27）

## 1. API 功能
`TypeVariable<D extends GenericDeclaration>` 表示类型变量（类/方法/构造器声明的 `<T>`）：
- `getName()`：声明名（T/U/V）。
- `getBounds()`：上界数组（无显式界时为 `[Object]`）。
- `getGenericDeclaration()`：声明处（Class/Method/Constructor）。
- `getAnnotatedBounds()`：带注解的界（`AnnotatedType[]`）。
- 实现 `Type` 与 `AnnotatedElement`。

## 2. Java 用户如何使用
`class.getTypeParameters()/method.getTypeParameters()` 遍历 → 名字匹配 + 界约束检查（`getBounds` 递归含 TV 自身的 PT）→ 泛型解析器（IDE、序列化框架、校验器）重建签名。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/TypeVariable/`（`GetTypeParameters.java` 等）：类/方法级变量名与界。
- JDK 自测 `Generics/StringsAndBounds.java`：界字符串与自引用界。
- Spring `ResolvableType.getTypeVariable/resolveVariable`：按声明处回溯解析变量（同构场景 1/3）。
- Hibernate Validator `TypeVariableUtil` 邻接实现（界驱动校验）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`TypeVariableTest.testBlackBox`，固定类 `Sample<T extends Number & Comparable<T>>`/`Box<T, U extends Number & Serializable>`/泛型方法 `echo<V>`）：
1. 界精确：无界 T → `[Object]`；U 双界 `[Number, Serializable]` 顺序稳定；名字/typeName/toString/equals。
2. 自引用界：`Comparable<T>` 为 PT 且实参即该 TV 自身、typeName 精确。
3. 方法级变量：V 由方法声明、界 `[Object]`、泛型返回/参数即该 TV。
4. 构造器：自身零类型参数、参数引用类级 T。
5. 注解界镜像：`getAnnotatedBounds` 长度与类型一致、无注解时空；TV 是 AnnotatedElement 的注解查询。
白盒保留原有多界断言 + 接口结构。产出：`TypeVariableTest.java`（36 断言通过）。

## 深化批次（2026-11-05）
新增三方法（37→62 断言）：
1. `testParameterizedBound`：`U extends List<String>` 界本身是 PT（raw List + arg String 递归）、annotatedBounds[0].getType() 与 getBounds() 同一对象——补参数化界维度（原版界全是 Class）。
2. `testAnnotatedBoundsContracts`：自造 RUNTIME `@Mark`（TYPE_USE+TYPE_PARAMETER）实测——`T extends @Mark Number` 界注解运行时可见；`<@Mark T>` 参数位注解**不**落在界上；双界 `@Mark Number & @Mark CharSequence` 各自独立携带注解。
3. `testConstructorAndSameNameVariables`：泛型构造器 `<E extends CharSequence>` 声明者==那个 Constructor、普通构造器空数组；类 TV `T` 与同名方法 TV `T` 不等且对称。

