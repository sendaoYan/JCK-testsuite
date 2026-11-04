# java.lang.reflect.GenericDeclaration — 测试设计（JDK-27）

## 1. API 功能
`GenericDeclaration`（`Class`、`Method`、`Constructor` 实现的接口）：
- 唯一抽象方法 `getTypeParameters()`：返回声明处的类型变量数组（`TypeVariable<?>[]`；非泛型声明返回空数组而非 null）。
- 继承 `AnnotatedElement`。

## 2. Java 用户如何使用
对三种声明体统一询问"声明了哪些类型变量"：`getTypeParameters()` → 按名取 `TypeVariable` → 读界。泛型解析框架（Spring `ResolvableType` 变量回溯）与编译器/校验器依赖此统一入口；区分类级与方法级作用域是关键。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/TypeVariable/GetTypeParameters.java`：类/方法/构造器三处的 `getTypeParameters` 行为。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/GenericDeclaration.java` 与 `Executable.getTypeParameters` 实现。
- Spring `ResolvableType.resolveVariable`：沿 `GenericDeclaration` 回溯变量绑定（同构场景 5）。
- Hibernate Validator/注解处理器：方法级变量界驱动校验（场景 3）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`GenericDeclarationTest.testBlackBox`，固定类 `Pair<K,V>`/`Plain` 与静态泛型方法）：
1. 类级声明：双变量按序（K/V）、声明处为该类、无界默认 `[Object]`。
2. 非泛型类/非泛型构造器返回空数组（非 null）。
3. 方法级声明：`<A extends Number, B>` 名字按序 + 界精确 + 声明处为该方法。
4. 泛型类构造器：自身零类型参数、参数引用类级 K。
5. 泛型类上的泛型方法：R 属方法作用域且与类级 K 不等（作用域隔离）。
6. `Class/Method/Constructor` 皆实现该接口（isAssignableFrom 三连）。
白盒保留原有类/方法变量断言 + 单抽象方法结构。产出：`GenericDeclarationTest.java`（25 断言通过）。
