# javax.management.MalformedObjectNameException 测试设计

## 1. API 功能
`MalformedObjectNameException` 继承 `OperationsException`。构造器：无参、`(String message)`。
由 `ObjectName` 解析非法名称时抛出（真实生产路径）。

## 2. Java 用户如何使用
```java
try {
    ObjectName name = new ObjectName("bad:::name");
} catch (MalformedObjectNameException e) {
    System.err.println(e.getMessage()); // Invalid character ':' in key part of property
}
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/ObjectName.java` 在解析过程中抛出本异常；
`test/jdk/javax/management/ObjectName/*` 大量用例断言非法名称抛出的精确消息。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、直接父类 `OperationsException`、可序列化、无参与 `(String)` 构造器。
- 黑盒：String 构造消息往返、无参消息 null、`new ObjectName("bad:::name")` 真实路径抛出并断言精确消息 `Invalid character ':' in key part of property`。
- 确定性：固定非法名称字符串；消息经探针实测稳定后写入。

## 深化（2026-11）
追加维度：构造链全重载、直接父类断言、serialVersionUID、序列化 round-trip、真实触发点。
- 构造：`()`/`(String)`，null String → null message；直接父类 OperationsException。
- serialVersionUID = `-572689714442915824L`（ObjectStreamClass.lookup 实测；JDK 源声明值，两侧同值）。
- 真实触发：`new ObjectName("a=b:c")` → 消息 `Unterminated key property part`；`new ObjectName("d:k=\"bad\\escape\"")` → `Invalid escape sequence '\e' in quoted value`；`new ObjectInstance("no-colon-here","c")` 同异常。触发点全集与消息矩阵见 ObjectName 目录测试。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
