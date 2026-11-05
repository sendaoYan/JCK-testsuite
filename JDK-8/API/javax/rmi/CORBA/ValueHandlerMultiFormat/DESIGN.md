# javax.rmi.CORBA.ValueHandlerMultiFormat 测试设计（JDK-8 侧）

## 1. API 功能
`ValueHandler` 的多流格式扩展：`getMaximumStreamFormatVersion()` —— 本 JDK 固定为 `(byte)2`。

## 2. Java 用户如何使用
```java
if (vh instanceof ValueHandlerMultiFormat) {
    byte max = ((ValueHandlerMultiFormat) vh).getMaximumStreamFormatVersion();  // 2
}
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口恰 1 个自有方法；继承 ValueHandler 面。
- 黑盒：`getMaximumStreamFormatVersion()==(byte)2` 精确哨兵；实现类（Util 委托链取到的
  ValueHandlerImpl）满足 `instanceof ValueHandlerMultiFormat`。
- 实现见 `ValueHandlerMultiFormatTest.java`。
