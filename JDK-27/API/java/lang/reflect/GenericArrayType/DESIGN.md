# java.lang.reflect.GenericArrayType — 测试设计（JDK-27）

## 1. API 功能
`GenericArrayType` 表示元素类型为参数化类型或类型变量的泛型数组（`List<String>[]`、`T[]`）：
- `getGenericComponentType()`：分量类型（PT/TV/再嵌套 GAT）。
- 继承 `Type`：`getTypeName/toString/equals/hashCode`。
- 注意：`String[]` 等普通数组直接是 `Class`，不是 GAT。

## 2. Java 用户如何使用
`field.getGenericType()/method.getGenericReturnType()` instanceof GAT → 递归取分量重建完整签名（`T[][]` 逐层剥离）；用于泛型解析器与序列化框架的数组维度重建。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Generics/TestPlainArrayNotGeneric.java`：普通数组不是 GAT 的判定（对应场景 4）。
- JDK 自测 `Generics/SignatureTest.java`：GAT 分量与签名串。
- Spring `ResolvableType.getArrayType/asArray`：泛型数组解析同构（场景 2/5）。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/generics/reflectiveObjects/GenericArrayTypeImpl.java`：typeName/equals 权威实现。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`GenericArrayTypeTest.testBlackBox`，固定类 `Holder<T>`/`Sample` 与静态泛型方法 `make`）：
1. `List<String>[]`：分量为 PT（raw List、arg String）、typeName/toString 精确。
2. `T[]`：分量为 TV（名 T、声明处即外层类）、typeName "T[]"。
3. `List<T>[]`：分量 PT 的实参为 TV、typeName 精确。
4. 普通数组 `String[]` 是 Class 非 GAT；`T[][]` 先剥一层仍为 GAT（"T[]"）再到底 TV。
5. 方法泛型返回类型为 GAT + `equals/hashCode` 值语义。
白盒保留原有分量判定 + 接口结构。产出：`GenericArrayTypeTest.java`（25 断言通过）。
