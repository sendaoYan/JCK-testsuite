# javax.management.Attribute 黑盒行为测试设计

## 1. API 功能
- 构造：`Attribute(String name, Object value)`，name 不能为 null。
- 查询：`getName()`、`getValue()`。
- 契约：`equals`/`hashCode` 基于 name+value；`toString` 包含二者。

## 2. Java 用户如何使用
MBeanServer 的 `getAttribute`/`setAttribute` 用 Attribute 打包属性名和值；用户构造后传给 setAttribute 或从 getAttributes 结果里拆包。
```java
mbs.setAttribute(on, new Attribute("Count", 42));
Attribute a = (Attribute) mbs.getAttribute(on, "Count");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/AttributeListTypeSafeTest.java`：Attribute/AttributeList 的用法。
- `test/jdk/javax/management/Introspector/*Test.java`：属性读写路径。
- 实现权威：`src/java.management/share/classes/javax/management/Attribute.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getName/getValue 返回构造入参；equals/hashCode 对称且对 name/value 敏感；null value 合法且相等性成立；null name 抛 RuntimeOperationsException（源码确认包裹 IllegalArgumentException）。
- 白盒：public 具体类、implements Serializable、含 getName/getValue/equals/hashCode/toString。

## 深化（2026-11）
追加维度：null-name 精确契约、serialVersionUID、序列化矩阵。
- `new Attribute(null, v)` → `RuntimeOperationsException`（包装 `IllegalArgumentException("Attribute name cannot be null ")`，外层 message 为 null）——两侧一致。
- serialVersionUID = `2484220110589082382L`（ObjectStreamClass.lookup 实测，两侧同值）。
- 序列化 round-trip：Integer 值 / String 值 / null 值三种，equals、getName、getValue 逐项回读断言。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
