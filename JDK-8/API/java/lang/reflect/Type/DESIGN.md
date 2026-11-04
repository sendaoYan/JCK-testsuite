# java.lang.reflect.Type — 测试设计（JDK-8）

## 1. API 功能
`Type` 是所有类型的公共标记接口（`Class`、`ParameterizedType`、`TypeVariable`、`WildcardType`、`GenericArrayType` 的父接口）：
- 唯一方法 `getTypeName()`（JDK 8 起带默认实现）：`Class` 走 `getName()` 语义（二进制名、`int`、`void`、数组 `java.lang.String[]`），各反射实现返回含类型实参的描述串。

## 2. Java 用户如何使用
`field.getGenericType()/method.getGenericReturnType()` 返回 `Type` → `instanceof` 分派到五种实现之一 → 取 `getTypeName()` 做签名展示或序列化（Gson/Jackson 泛型解析、Hibernate 类型绑定、IDE 类型提示）。

## 3. 参考来源
- Oracle javadoc JDK 8 `java.lang.reflect.Type`（getTypeName 默认实现 = `getName()` 或含实参描述）。
- JDK 源码 `sun/reflect/generics/reflectiveObjects/*Impl.toString/typeName`（精确串权威）。
- javap 双侧枚举：接口方法集双侧一致，仅 `getTypeName` 一个声明方法。
- OpenJDK 回归 `test/jdk/java/lang/reflect/Generics/SignatureTest.java`（签名串等价类思路，代码自写）。

## 4. 测试用例设计 + 易错规范细节
黑盒场景（自造样本 `Sample/Deep/Box<T>/Enclosing.Inner`）：
| # | 维度 | 断言要点 |
|---|------|----------|
| 1 | 标记语义 | 五种实现（Class/PT/TV/WC/GAT）各经自造样本收集进 `Object[]` 逐个 `instanceof Type`；primitive/数组/void 的 Class 也是 Type |
| 2 | 负例形态 | Class 不是 PT、通配符不是 PT、TV 不是 GAT（跨实现互斥） |
| 3 | getTypeName 精确串 | `int`/`void`/`java.lang.String`（==`getName()`）/`java.lang.String[]`/`java.lang.String[][]` |
| 4 | 嵌套类 | 自造 `Enclosing.Inner` 的 typeName == getName()，以 `$` 分隔；`java.util.Map$Entry` 同理（易错：不是 `Map.Entry` 点分） |
| 5 | 反射形态串 | `java.util.List<java.lang.String>`、嵌套 `java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>`（含前缀片段）、`T[]`、`java.util.List<java.lang.String>[]`、`? extends java.lang.Number`、`T` |
| 6 | toString 一致性 | 四种反射形态 toString == getTypeName |
| 7 | 数组负例 | `String[]`/`int[]` 字段 getGenericType 是 Class 非 GAT；重复查找 equals |
| 8 | 接口面 | getDeclaredMethods()==1 且即 getTypeName、isDefault、返回 String、零参；五个子接口均 assignable from Type |
易错规范细节：数组元素类型名 `[]` 后缀；嵌套类二进制名 `$`；raw 使用（无泛型签名字段）得到的是 Class 而非 PT；`? extends X` 前后空格精确。产出：`TypeTest.java`（59 断言通过）。
