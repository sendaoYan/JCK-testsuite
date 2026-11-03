# SerialJavaObject 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialJavaObject` 包装任意 Java 对象，供断开的 RowSet 序列化（JDK 源码 `SerialJavaObject.java`）。

- 构造器：`SerialJavaObject(Object)`。null 对象在 `obj.getClass()` 处抛 NPE；非 Serializable 对象不抛异常，只记录 RowSetWarning；静态公有字段理论上抛 SerialException，但实现用 `getModifiers() == Modifier.STATIC` 判定，而 `Class.getFields()` 只返回 public 字段（必含 PUBLIC 位），该判定实际永远为 false，故该路径为死代码（`test/jdk` 相关测试 test01 也被 disabled）。
- 读取：`getObject()`——**返回内部保存的同一引用**（非反序列化副本）；`getFields()`——返回 `obj.getClass().getFields()`（公有字段）。
- 对象语义：`equals`（委托 `obj.equals`）、`hashCode`（`31 + obj.hashCode()`）、`clone`（浅拷贝 fields、复制 chain）。

## 2. Java 用户如何使用
```java
SerialJavaObject sj = new SerialJavaObject(someSerializable);
Object o = sj.getObject();        // 与 someSerializable 同一引用
Field[] f = sj.getFields();       // 公有字段
SerialJavaObject c = (SerialJavaObject) sj.clone();
```
组合链路：`new(obj) → getObject → getFields → equals/hashCode → clone`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialJavaObjectTests.java`：验证 null 抛 NPE、`getFields` 等于 `obj.getClass().getFields()`、`getObject` 等于原对象、clone/序列化相等。
- JDK 实现 `SerialJavaObject.java`：确认 `getObject` 直接 `return this.obj`；非 Serializable 只 `setWarning` 不抛异常。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialJavaObjectTest.testBlackBox`）覆盖：
- 主链路：Serializable 对象 getObject 相等且同一引用 → getFields 暴露公有字段 → equals/hashCode/clone。
- 边界：非 Serializable 对象被接受并返回；null 对象抛 NPE（实现行为）。
- 用自定义 Serializable 类型（含公有字段）；headless，不加 `@key`。
