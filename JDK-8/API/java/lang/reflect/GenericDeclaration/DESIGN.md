# java.lang.reflect.GenericDeclaration — 测试设计（JDK-8）

## 1. API 功能
`GenericDeclaration` 标记能声明类型变量的构造：
- 唯一方法 `getTypeParameters()` 返回 `TypeVariable<?>[]`（非泛型声明返回空数组，**非 null**）。
- JDK 8 公开实现者：`Class`（含接口/枚举/数组类/原始类型数组类）、`Method`、`Constructor`。继承 `AnnotatedElement`。

## 2. Java 用户如何使用
泛型元数据工具遍历 `Class/Method/Constructor` 的 `getTypeParameters()` 推断泛型 arity、校验签名（如 `@FunctionalInterface` 检查器、DI 容器按类型参数解析 bean）；`TypeVariable.getGenericDeclaration()` 反向定位声明位置。

## 3. 参考来源
- Oracle javadoc JDK 8 `java.lang.reflect.GenericDeclaration`（空数组契约）。
- JLS 8 §8.1.2（类类型参数）、§8.4.4（方法类型参数）、§8.8.4（构造器类型参数——构造器可声明自己的类型变量）、§8.9（枚举**不允许**声明类型参数）。
- javap 双侧枚举：1 个抽象方法双侧一致。

## 4. 测试用例设计 + 易错规范细节
黑盒场景（自造 `Sample<T extends Number>`、`Pair<K,V>`（含 `<E extends CharSequence>` 泛型构造器）、`interface Numbered<I extends Number>`、`enum Color`、`cast<A extends Number & CharSequence, B>` 方法）：
| # | 维度 | 断言要点 |
|---|------|----------|
| 1 | 实现者矩阵 | Class/Method/Constructor 均 isAssignableFrom；数组类、原生数组类、枚举类、普通类实例均 instanceof GenericDeclaration |
| 2 | 空数组契约 | 非泛型类/枚举/数组类/泛型接口本体 getTypeParameters() 非 null 且长度 0——易错：是空数组不是 null |
| 3 | 类/接口级 | Pair 双变量有序（K,V）、无界隐含 Object 界；接口 `Numbered<I extends Number>` 界精确、声明者回环 == 接口本身 |
| 4 | 方法级 | `cast<A,B>` 双变量、A 双界按声明顺序（Number, CharSequence）、B 隐含 Object；变量声明者 == 那个 Method（回环） |
| 5 | 构造器级 | 泛型构造器 `<E> Pair(E)` 有自己的 TV（名/界/声明者==Constructor）；普通构造器空——易错：构造器可声明类型参数但通常被忽略 |
| 6 | 回环 | `gd.getTypeParameters()[i].getGenericDeclaration() == gd` 对类/方法双向成立 |
易错规范细节：枚举语法上不能声明类型参数（但枚举 Class 仍是 GenericDeclaration、返回空数组）；构造器类型参数与所属类类型参数是两个作用域、同名也不相等。产出：`GenericDeclarationTest.java`（43 断言通过）。
