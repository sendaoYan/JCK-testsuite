# java.lang.reflect.GenericArrayType — 测试设计（JDK-8）

## 1. API 功能
`GenericArrayType` 表示组件类型含类型变量或参数化类型的数组（`T[]`、`List<String>[]`）：
- 唯一方法 `getGenericComponentType()` 返回组件 `Type`。无公开构造，经泛型字段/方法签名取得。注意：`String[]`/`int[]` 等非泛型组件数组是普通 `Class`，不是 GAT。

## 2. Java 用户如何使用
泛型容器序列化框架对 `T[]`/`List<String>[]` 字段分派 GAT 剥组件类型，递归解析元素类型（数组反序列化器注册）；`Method.getGenericReturnType()` 对泛型数组方法返回 GAT。

## 3. 参考来源
- Oracle javadoc JDK 8 `java.lang.reflect.GenericArrayType`（组件是 TV 或 PT；数组类本身不是 GAT）。
- JDK 源码 `sun/reflect/generics/reflectiveObjects/GenericArrayTypeImpl`（equals 按组件、typeName 组件名+`[]`）。
- JLS 8 §10.1（数组类型与泛型数组创建限制）；javap 双侧枚举：1 个抽象方法双侧一致。

## 4. 测试用例设计 + 易错规范细节
黑盒场景（自造 `Holder<T>` 含 `T[]`/`T[][]`/`List<String>[]`/`List<?>[]`/`String[]`/`int[]` 字段、静态泛型方法 `make`/`take`）：
| # | 维度 | 断言要点 |
|---|------|----------|
| 1 | PT 组件 | `List<String>[]` 组件是 PT（raw List、arg String）；typeName `java.util.List<java.lang.String>[]`、toString==typeName |
| 2 | TV 组件 | `T[]` 组件 instanceof TypeVariable 且 **==（同一对象）** `Holder.getTypeParameters()[0]`——易错：是同一实例不是等价实例 |
| 3 | 多维 | `T[][]` 逐层剥：外层组件仍是 GAT（`T[]`），最内组件是那个 TV；typeName `T[][]` |
| 4 | 通配数组 | `List<?>[]` 组件 PT 的实参是 WildcardType；typeName `java.util.List<?>[]` |
| 5 | 方法签名 | 返回值 `T[]` 与参数 `T[]` 都是 GAT、typeName 精确 |
| 6 | 负例 | `String[]`/`int[]` 字段 getGenericType 是 Class 非 GAT——易错：原生类型数组绝不是 GAT |
| 7 | equals/hashCode | 同签名两次获取 equals+hashCode；反射律；equals(null) false；不同组件不等；跨类同形状（两处 `List<String>[]`）equals |
易错规范细节：typeName 恒以 `[]` 结尾；GAT 组件不可能是 Class（那样整个数组就是 Class）。产出：`GenericArrayTypeTest.java`（40 断言通过）。
