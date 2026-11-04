# java.lang.reflect.WildcardType — 测试设计（JDK-27）

## 1. API 功能
`WildcardType` 表示泛型通配符（`?`/`? extends X`/`? super Y`）：
- `getUpperBounds()`：上界数组（无界与 `super` 的隐式上界为 `[Object]`）。
- `getLowerBounds()`：下界数组（`extends`/无界为空）。
- 继承 `Type`：`getTypeName/toString/equals/hashCode`。

## 2. Java 用户如何使用
从 `ParameterizedType.getActualTypeArguments()` 取出 → 按上/下界做类型兼容判定（能否写入/读取）：`? extends Number` 只读、`? super Integer` 只写。泛型容器 API 校验、JSON 反序列化目标类型推断均依赖此。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Generics/SignatureTest.java`/`Probe.java`：通配符上下界的精确断言。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/generics/reflectiveObjects/WildcardTypeImpl.java`：隐式 Object 上界与相等语义权威实现。
- Spring `ResolvableType.getBounds` 邻接实现：通配界驱动的 assignability 判定（场景 3 同构）。
- Gson/Jackson 泛型目标解析：`? extends` 界决定元素反序列化类型（场景 4）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`WildcardTypeTest.testBlackBox`，固定类 `Sample/More` 与方法 `consume`）：
1. 无界 `?`：隐式 `[Object]` 上界、零下界、typeName "?"。
2. 上界/下界通配：精确类型（Number/Integer）、隐式 Object、typeName 精确。
3. 单 PT 双端通配：`Map<? extends String, ? super Long>` 键值界分别断言 + 整体 typeName。
4. 方法签名中的通配与字段侧行为一致。
5. `equals/hashCode` 值语义：重复查找相等、不同界不等、同形通配相等。
白盒保留原有上下界断言 + 接口结构。产出：`WildcardTypeTest.java`（30 断言通过）。
