# java.lang.reflect.Type — 测试设计（JDK-27）

## 1. API 功能
`Type` 是所有类型的公共标记接口（`Class`、`ParameterizedType`、`TypeVariable`、`WildcardType`、`GenericArrayType` 的父接口）：
- 唯一默认方法 `getTypeName()`：Class 走 `getName()` 语义（`int`/`java.lang.String[]`），各反射实现给出含类型实参的描述。

## 2. Java 用户如何使用
`field.getGenericType()/method.getGenericReturnType()` 返回 `Type` → `instanceof` 分派到五种实现之一 → 取 `getTypeName()` 做签名展示或序列化（Gson/Jackson 泛型解析、Hibernate 类型绑定）。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Generics/SignatureTest.java`/`StringsAndBounds.java`：经 `Type` 断言签名与类型名字符串。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/Type.java` 及 `sun/reflect/generics/reflectiveObjects/*`（typeName 实现权威）。
- Gson `TypeToken`：`getTypeName/instanceof` 分派解析泛型（对应场景 2）。
- Spring `ResolvableType`/`GenericTypeResolver`：同构分派模型。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`TypeTest.testBlackBox`，固定类 `Sample/GenericBox<T,U>`/静态泛型方法 `swap`）：
1. Class typeName：`int`/`java.lang.String`/一维与二维数组/`void` 精确串。
2. 五种反射形态 typeName 精确串：`List<String>` PT、嵌套 `Map<String,List<Integer>>`、`? extends Number` 通配、`T[]` 与 `List<String>[]` GAT、`T` TV。
3. `toString` 与 `getTypeName` 一致性。
4. 类型经重复查找等价；泛型方法参数共享同一方法级类型变量（X）。
5. 普通数组 `String[]` 的 genericType 即 `Class` 而非 GAT。
白盒保留原有标记接口断言 + getTypeName 默认方法检查。产出：`TypeTest.java`（31 断言通过）。
