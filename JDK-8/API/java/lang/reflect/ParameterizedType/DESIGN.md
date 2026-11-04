# java.lang.reflect.ParameterizedType — 测试设计（JDK-8）

## 1. API 功能
`ParameterizedType` 表示参数化类型（`List<String>`、`Map.Entry<K,V>`）：
- `getRawType()` 原始类型；`getActualTypeArguments()` 实参数组（递归：元素可为 PT/TV/WildcardType/Class）；`getOwnerType()` 内部类属主（顶层 null）。无公开构造，经字段/方法/超类泛型签名取得。

## 2. Java 用户如何使用
`Field.getGenericType()`/`Method.getGenericReturnType()`/`Class.getGenericSuperclass()`/`getGenericInterfaces()` 返回后向下转型 PT，递归取实参做泛型注入（Spring）、反序列化目标类型（Gson `TypeToken`）、超类型 token 模式。

## 3. 参考来源
- Oracle javadoc JDK 8 `java.lang.reflect.ParameterizedType`（owner 语义、args 与 raw 的类型参数数对应）。
- JDK 源码 `sun/reflect/generics/reflectiveObjects/ParameterizedTypeImpl`（equals/hashCode 值语义、Map.Entry owner==Map）。
- JLS 8 §4.5（参数化类型）；javap 双侧枚举：3 个抽象方法双侧一致。

## 4. 测试用例设计 + 易错规范细节
黑盒场景（自造 `Map<String,List<Integer>>` 嵌套字段、`Map.Entry<String,Integer>`、`Enclosing.Inner<Q>` 内部类、`G<T,U> implements Comparable<G<T,U>>`、`SubG extends G<Integer,ArrayList<String>>`、raw 字段）：
| # | 维度 | 断言要点 |
|---|------|----------|
| 1 | 嵌套递归 | 外层 raw Map + arg0 String；arg1 是内层 PT（raw List、arg Integer）——易错：内层不是 Class |
| 2 | owner | `Map.Entry<..>` owner==Map.class、raw==Map.Entry.class；自造非静态内部类 `Enclosing.Inner<String>` owner==Enclosing.class；顶层类型 owner null |
| 3 | 泛型超类 | `class SubG extends G<Integer,ArrayList<String>>` 经 getGenericSuperclass() 断言 raw/args（第二个实参本身是 PT 再递归）；`StringList extends ArrayList<String>` 同 |
| 4 | 泛型接口 | `G implements Comparable<G<T,U>>` 的 getGenericInterfaces()[0]：raw Comparable、实参是含 TV T/U 的递归 PT |
| 5 | equals/hashCode | 两次获取 equals+hashCode 一致、反射律、equals(null) false、不同参数化不等 |
| 6 | 负例 | raw 使用（`List raw` 字段）getGenericType()==List.class 非 PT——易错：无泛型签名得到 Class |
| 7 | 通配实参 | `Class<?>` 实参 instanceof WildcardType、typeName `?` |
| 8 | 串精确 | 嵌套 typeName 全串 + 前缀/内层片段 contains；toString==typeName |
产出：`ParameterizedTypeTest.java`（52 断言通过）。
