# SerialRef 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialRef` 是 `java.sql.Ref` 的离线可序列化实现，保存 SQL REF 的基类型名与引用对象（JDK 源码 `SerialRef.java`）。

- 构造器：`SerialRef(Ref)`（ref 为 null 或 `ref.getBaseTypeName()` 为 null 抛 SQLException）。
- 读取：`getBaseTypeName()`、`getObject()`（优先委托底层 Ref.getObject()）、`getObject(Map)`（返回 `map.get(内部 object)`）。
- 修改：`setObject(Object)`——**实际是支持的**，委托底层 `ref.setObject(obj)` 并更新内部 object（JDK 源码如此，`test/jdk` 的 `SerialRefTests.test06` 也验证了 setObject 后 getObject 变化）。
- 对象语义：`equals`、`hashCode`、`clone`（clone 置底层 reference 为 null）。

## 2. Java 用户如何使用
```java
Ref r = ...; // 驱动提供的 Ref
SerialRef sr = new SerialRef(r);
String tn = sr.getBaseTypeName();
Object o = sr.getObject();
sr.setObject(newValue);              // 更新底层引用
SerialRef copy = (SerialRef) sr.clone();
```
组合链路：`new(Ref) → getBaseTypeName → getObject → setObject → getObject → clone/equals`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialRefTests.java`：验证 null Ref / null baseTypeName 抛 SQLException、`getBaseTypeName`、`getObject`、`setObject` 改变引用对象、`clone` 相等。
- JDK 实现 `SerialRef.java`：确认 `setObject` 委托底层 `reference.setObject`；`getObject()` 优先走 `reference.getObject()`；`getObject(Map)` 返回 `map.get(object)`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialRefTest.testBlackBox`）覆盖：
- 主链路：`new(StubRef)` → getBaseTypeName → getObject → setObject → getObject 反映更新 → clone/equals/hashCode。
- 边界：`getObject(Map)` 空 map 返回 null（内部 object 为 Ref，map 不含该键）。
- 异常路径：null Ref、null baseTypeName 抛 SQLException。
- 用内存 `StubRef`；headless，不加 `@key`。
