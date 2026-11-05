# javax.rmi.CORBA.Util 测试设计（JDK-8 侧）

## 1. API 功能
RMI-IIOP 工具门面（静态方法 + `utilDelegate` 委托）。
- 类加载：`loadClass/loadClass...`；异常包装 `wrapException`；映射 `mapSystemException`;
  远程值 `isLocal/getLocalHost/writeRemote/writeAbstract/writeAny/copyObject(s)`; servant 生命周期
  `registerTarget/unregisterTarget/exportObject/unexportObject`。

## 2. Java 用户如何使用
```java
Class<?> c = Util.loadClass("[Ljava.lang.String;", null, null);  // RMI 数组描述符 → String[].class
RemoteException re = ...;
throw (RuntimeException) Util.wrapException(re);   // RemoteException → ServerException 包装
Util.unexportObject(tie);                          // NoSuchObjectException("Tie not found") 若无
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/javax/rmi/CORBA/*.java` 与实现 `com/sun/corba/se/impl/javax/rmi/CORBA/`（委托机制与 ORB 常量的权威定义）。
- 委托族共享同一装配模式：首次门面调用把 `*Delegate` 字段装配为 `com.sun.corba.se.impl...` 实现。
## 4. 测试用例设计 + 自动化实现
- 白盒：15 个公共静态方法精确面；`utilDelegate` 字段反射闭环（null → 首调用装配为
  `com.sun.corba.se.impl.javax.rmi.CORBA.Util`）。
- 黑盒：`loadClass("[Ljava.lang.String;")==String[].class`（RMI 描述符形式）；
  wrapException 精确映射矩阵（RuntimeException 原样同一对象 / RemoteException→ServerException /
  Error→ServerError / 受检 Exception→UnexpectedException，cause 全保留）；
  `unexportObject(null)`→NoSuchObjectException 且消息精确 `"Tie not found"`。
- 实现见 `UtilTest.java`。
