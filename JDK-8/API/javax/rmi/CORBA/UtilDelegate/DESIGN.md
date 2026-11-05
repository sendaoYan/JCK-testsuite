# javax.rmi.CORBA.UtilDelegate 测试设计（JDK-8 侧）

## 1. API 功能
`Util` 的委托接口：与 `Util` 公共面完全同构的 15 个方法，由 `utilDelegate` 装配实现。

## 2. Java 用户如何使用
```java
// 用户不直接调用；插入自定义实现的入口：
// 系统属性 javax.rmi.CORBA.UtilClass 指定实现类名，Util 首调用时反射装配。
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面 == Util 公共面（15 方法逐一同名同参）；`Util.utilDelegate` 字段当前值即为
  UtilDelegate 的实现类（反射读字段比对类型）。
- 黑盒：经接口引用调用 `wrapException`/`unexportObject` 与门面行为一致（同一实现对象）。
- 实现见 `UtilDelegateTest.java`。
