# javax.rmi.CORBA.PortableRemoteObjectDelegate 测试设计（JDK-8 侧）

## 1. API 功能
`PortableRemoteObject` 的委托接口（5 方法）：connect/exportObject/unexportObject/toStub/narrow。

## 2. Java 用户如何使用
```java
// 系统属性 javax.rmi.CORBA.PortableRemoteObjectClass 指定实现；默认装配到 CORBA 实现。
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：5 方法精确面 == PortableRemoteObject 公共面；`PortableRemoteObject.proDelegate` 字段环
  （反射读字段 → 实现类类型锁定）。
- 黑盒：经委托引用的 narrow(null)→null 与门面一致。
- 实现见 `PortableRemoteObjectDelegateTest.java`。
