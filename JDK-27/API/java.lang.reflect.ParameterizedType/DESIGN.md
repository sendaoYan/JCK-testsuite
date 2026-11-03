# java.lang.reflect.ParameterizedType — 测试设计（JDK-27）

## 1. API 功能
`ParameterizedType` 表示参数化类型（`List<String>`、`Map.Entry<K,V>` 的声明侧视图）：
- `getRawType()`：原始类型（`List`）。
- `getActualTypeArguments()`：类型实参数组（可为 Class/TV/通配/嵌套 PT/GAT）。
- `getOwnerType()`：内部类型的宿主（`Map.Entry` 的 `Map`），顶层为 null。
- 继承 `Type`：`getTypeName/toString/equals/hashCode`。

## 2. Java 用户如何使用
`field.getGenericType()/class.getGenericSuperclass()` 强转 `ParameterizedType` → 读 raw+args 还原完整泛型签名；`getOwnerType` 区分内部类型。典型：泛型 DAO 基类取实体类型（`getGenericSuperclass`）、JSON 泛型反序列化、Hibernate 元模型。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Generics/TestParameterizedType.java`：raw/args/owner 断言。
- JDK 自测 `Generics/HashCodeTest.java`：`equals/hashCode` 值语义。
- Gson `TypeToken.getGenericSuperclass` 惯用法：`new TypeToken<List<String>>(){}` 经 `getGenericSuperclass` 提取实参（同构场景 5）。
- Spring `ResolvableType.forField(...).getGeneric(0)`：raw+args 递归解析（场景 2/3）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ParameterizedTypeTest.testBlackBox`，固定类 `Types`/`Outer.Inner<Q>`/`StringList`）：
1. 简单 PT：raw=List、单实参 String、无 owner、typeName 精确。
2. 嵌套 PT：`Map<String,List<Integer>>` 递归 raw/arg、整体 typeName 精确。
3. owner：`Map.Entry`（owner=Map）与非静态 `Outer.Inner<Q>`（owner=Outer）。
4. 通配实参：`Class<?>` 的 arg 为 WildcardType、名字 "?"。
5. `getGenericSuperclass`：`StringList extends ArrayList<String>` 参数化超类。
6. `equals/hashCode/toString` 值语义与重复查找一致性。
白盒保留原有 Map 字段断言 + 三抽象方法接口结构。产出：`ParameterizedTypeTest.java`（33 断言通过）。
