# javax.rmi.CORBA.Tie 测试设计（JDK-8 侧）

## 1. API 功能
RMI-IIOP servant 端接口：`InvokeHandler` + `org.omg.CORBA.portable.ResponseHandler` 组合面。
- 方法：`thisObject()/delegate(Delegate)/orb()/orb(ORB)/_invoke(String, InputStream, ResponseHandler)`
  /`target(Remote)/target()` —— 恰好 6 个自有声明。

## 2. Java 用户如何使用
```java
Tie tie = ...;                     // Util.registerTarget(tie, impl)
OutputStream reply = tie._invoke("echo", in, handler);   // 返回 OutputStream
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面 6 方法精确锁定（`_invoke` 返回 org.omg.CORBA.portable.OutputStream）；
  与 `InvokeHandler`/`ResponseHandler` 的可达性关系。
- 黑盒：默认 target() 为 null；thisObject()/orb() 未委托即用 → BAD_OPERATION 族负例。
- 实现见 `TieTest.java`。
