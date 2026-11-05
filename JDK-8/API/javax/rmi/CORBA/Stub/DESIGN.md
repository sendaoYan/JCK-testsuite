# javax.rmi.CORBA.Stub 测试设计（JDK-8 侧）

## 1. API 功能
所有 RMI-IIOP stub 的公共基类（`ObjectImpl` 子类）。
- 方法：`connect/hashes hashCode/equals/toString`（委托 `_orb()` 侧的 StubDelegate）；
  `_get_delegate` 族继承；序列化协议 `writeObject/readObject`。

## 2. Java 用户如何使用
```java
Stub s = (Stub) PortableRemoteObject.toStub(impl);
s.connect(orb);          // 连接到 ORB 后才能收发
s.hashCode();            // 未连接 → BAD_OPERATION("The delegate has not been set!")
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：`hashCode/equals/toString/connect` 四个自有声明 + writeObject/readObject 私有序列化协议；
  `_ids()` 未重新声明（继承）。
- 黑盒：未连接 stub 的 hashCode/equals → BAD_OPERATION 且消息精确 "The delegate has not been set!"；
  toString 含 "no delegate set"；`connect(null)`→RemoteException；序列化往返触发同一 BAD_OPERATION；
  `stubDelegate` 字段懒装配闭环（新 stub 字段 null → 首次 toString() 后装配）。
- 实现见 `StubTest.java`。
