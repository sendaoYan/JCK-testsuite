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
