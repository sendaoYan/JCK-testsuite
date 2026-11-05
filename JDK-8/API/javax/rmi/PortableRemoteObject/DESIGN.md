# javax.rmi.PortableRemoteObject 测试设计（JDK-8 侧）

## 1. API 功能
RMI-IIOP 的可移植 servant 入口（静态门面，内部委托 `PortableRemoteObjectDelegate`）。
- 方法：`connect/exportObject/unexportObject/toStub/narrow/delegate`。

## 2. Java 用户如何使用
```java
PortableRemoteObject.exportObject(new MyImpl());          // 导出为 IIOP 对象
MyRemote stub = (MyRemote) PortableRemoteObject.toStub(impl);
Object r = PortableRemoteObject.narrow(obj, MyRemote.class); // IIOP 下的类型收窄
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `javax/rmi/PortableRemoteObject.java` + `com/sun/corba/se/impl/javax/rmi/PortableRemoteObject.java`（委托实现）。
- 委托字段 `proDelegate` 的懒装配链是 `UtilDelegate`/`StubDelegate` 同族机制。
## 4. 测试用例设计 + 自动化实现
- 白盒：public final 类、私有构造器、5 个静态方法签名、`proDelegate` 字段的委托环（反射读字段 → 首次调用装配）。
- 黑盒负例矩阵：`narrow(null,X)`→null、`narrow(obj,String.class)`→ClassCastException、`narrow(obj,null)`→NPE、
  narrow 到已实现接口返回同一对象；`connect(null,null)`/`connect(remote,null)`→NPE、两个普通 remote connect 不抛；
  `exportObject(null)`→NPE、导出无 stub 的 remote → StubNotFoundException；
  `unexportObject(null)`→NPE、未导出对象 → NoSuchObjectException；`toStub(null)`→NPE、未导出 → NoSuchObjectException。
- 实现见 `PortableRemoteObjectTest.java`。
