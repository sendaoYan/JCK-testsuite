# javax.management.InvalidApplicationException 测试设计

## 1. API 功能
`InvalidApplicationException` 继承 `Exception`，携带一个非法的 application 对象（无 getter）。
构造器：`(Object val)`；可观察方法：toString()（默认）。

## 2. Java 用户如何使用
```java
throw new InvalidApplicationException(badAppObject);
// getMessage() == null; toString() -> "javax.management.InvalidApplicationException"
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/InvalidApplicationException.java` 及 Query 表达式求值路径；
JDK 自带 `test/jdk/javax/management` 的 Query 用例在构造非法查询表达式时触发这些异常。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、继承 `Exception`、可序列化、构造器与方法形态。
- 黑盒：构造后断言 `toString()` 精确内容、`getMessage()` 为 null（无 detail message）、`instanceof Exception`；。
- 确定性：固定值字符串/对象。

## 深化（2026-11）
追加维度：构造契约、serial 字段实证、层级（**不在 JMException 层次**）、serialVersionUID、序列化。
- 构造：仅 `(Object val)` 一个公开构造（javap 双侧实证）；**无公开 getVal()**——val 为私有串行化字段，通过 `ObjectStreamClass.lookup(...).getFields()` 断言（恰一个名为 `val`、类型 Object 的 serial field）。
- 层级：直接 extends java.lang.Exception，`JMException.class.isInstance(x)` 为 false（查询子表达式类错误的内部异常）。
- getMessage() 恒 null（构造器不把 val 当名息）。
- serialVersionUID = `-3048022274675537269L`（两侧同值）；String/Integer/ObjectName 三种 val 序列化 round-trip。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
