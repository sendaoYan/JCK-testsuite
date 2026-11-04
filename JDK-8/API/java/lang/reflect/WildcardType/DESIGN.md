# java.lang.reflect.WildcardType — 测试设计（JDK-8）

## 1. API 功能
`WildcardType` 表示通配符类型实参（`?`/`? extends B`/`? super B`）：
- `getUpperBounds()` 上界数组（无界与下界通配隐式 `[Object.class]`）；`getLowerBounds()` 下界数组（仅 `? super` 非空）。无公开构造，只能作为 `ParameterizedType.getActualTypeArguments()` 元素取得。

## 2. Java 用户如何使用
泛型解析框架（Gson/Spring `ResolvableType`）对 `List<? extends Number>` 之类的协变/逆变实参分派 `getUpperBounds/getLowerBounds` 推断元素类型；API 兼容性检查工具用界做型变判定。

## 3. 参考来源
- Oracle javadoc JDK 8 `java.lang.reflect.WildcardType`（上界前置 Object 摊平规则、返回数组可为空）。
- JDK 源码 `sun/reflect/generics/reflectiveObjects/WildcardTypeImpl`（equals 按 upper+lower 值语义）。
- JLS 8 §4.5.1（通配符只有一个可选上界+一个可选下界——语法上不允许 `? extends A & B` 多上界）。
- javap 双侧枚举：2 个抽象方法双侧一致。

## 4. 测试用例设计 + 易错规范细节
黑盒场景（自造字段 `List<? extends Number>`/`List<? super Integer>`/`List<?>`/`Map<String,List<? extends Number>>`/`List<?>[]`/`Class<?>`、方法 `consume(List<? super Number>)`）：
| # | 维度 | 断言要点 |
|---|------|----------|
| 1 | 上界通配 | upper==[Number.class]、lower 长度 0；typeName `? extends java.lang.Number` 精确 |
| 2 | 下界通配 | lower==[Integer.class]、upper 隐式 [Object.class]；typeName `? super java.lang.Integer` |
| 3 | 无界 | upper 隐式 [Object.class]、lower 空；typeName/toString 均为 `?` |
| 4 | 方法签名 | 方法参数位置的通配符行为与字段一致（`? super Number` 下界精确） |
| 5 | 嵌套/数组位 | Map 值位嵌套 PT 内通配符；`List<?>[]` 经 GAT→PT→arg 链取到通配符——易错：数组位置通配符要先剥 GAT |
| 6 | equals/hashCode | 同字段两次取 equals+hashCode 一致；同形状不同来源（`Class<?>` vs `List<?>` 实参）equals；不同界互不等；equals(null) false；反射律 |
| 7 | 交叉负例 | 上界通配 lower 恒空、下界通配 upper 恒单元素 Object |
易错规范细节：`? extends X` 串中 `? extends ` 后接全限定名；通配符不能与自身上界 Class 相等；Java 语法通配符最多一个显式上界（多上界仅类型变量可声明，不做该负例）。产出：`WildcardTypeTest.java`（46 断言通过）。
