# javax.rmi.CORBA.ValueHandler 测试设计（JDK-8 侧）

## 1. API 功能
自定义值类型序列化桥（6 个方法）：
`writeValue/writeReplace/getRMIRepositoryID/getRCodebase/isCustomMarshaled/Throwable` 族。

## 2. Java 用户如何使用
```java
String repid = vh.getRMIRepositoryID(String.class);  // "IDL:omg.org/CORBA/WStringValue:1.0"
String objid = vh.getRMIRepositoryID(Object.class);  // "RMI:java.lang.Object:0000000000000000"
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：6 方法精确签名面（含 writeReplace）。
- 黑盒：`getRMIRepositoryID` 精确表（String→IDL 形式、Object→RMI+零哈希、嵌套类 `$` 转义为 `\U0024`）；
  `isCustomMarshaled` 按类形状（externalizable vs serializable vs 非 Serializable→NPE 的实现观察值锁定）。
- 实现见 `ValueHandlerTest.java`。
