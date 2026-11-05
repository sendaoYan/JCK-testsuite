# javax.rmi.CORBA.StubDelegate 测试设计（JDK-8 侧）

## 1. API 功能
`Stub` 的委托接口：6 个方法（hashCode/equals/toString/connect/读写序列化），`Stub` 内部经
`stubDelegate` 调用。

## 2. Java 用户如何使用
```java
// 系统属性 javax.rmi.CORBA.StubClass 指定自定义实现；默认装配 com.sun.corba.se.impl.javax.rmi.CORBA.StubDelegateImpl。
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：6 方法精确面；`Stub.stubDelegate` 字段的懒装配实证（fresh stub 反射读为 null，
  首次门面调用后为 StubDelegateImpl 类型）。
- 黑盒：委托装配前后 `toString()` 行为变化（null delegate 描述 → 正常描述）。
- 实现见 `StubDelegateTest.java`。
