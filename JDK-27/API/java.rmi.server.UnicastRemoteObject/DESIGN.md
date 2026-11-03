# java.rmi.server.UnicastRemoteObject 行为测试设计

## 1. API 功能
- 具体类，`extends RemoteServer`；受保护构造器（自动导出到匿名端口）。
- 静态方法：`exportObject(Remote)`/`exportObject(Remote, int)`/`exportObject(Remote, int, RMIClientSocketFactory, RMIServerSocketFactory)`（含 ObjectInputFilter 重载）、`unexportObject(Remote, boolean)`。
- 实例方法：`clone()`。

## 2. Java 用户如何使用
- `UnicastRemoteObject.exportObject(obj, 0)` 导出到临时端口；`unexportObject(obj, true)` 反导出。
- 子类构造器自动导出；`RemoteObject.toStub(obj)` 取 stub。
- `clone()` 仅当子类实现 `Cloneable` 才成功（否则抛 CloneNotSupportedException）。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/server/UnicastRemoteObject/useDynamicProxies/UseDynamicProxies.java`：`exportObject(server, 0)` + `unexportObject(server, true)`。
- JDK 测试 `test/jdk/java/rmi/server/UnicastRemoteObject/keepAliveDuringCall/ShutdownImpl.java`：导出/反导出。
- 注意：RMI 导出会创建非 daemon 线程，必须在 finally 中 unexport 否则 JVM 不退出。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、父类 RemoteServer、具体类、导出/反导出静态方法签名。
- 黑盒：`exportObject(impl,0)` 返回 stub + `toStub` 非 null；`unexportObject` true；二次 unexport 抛 NoSuchObjectException；匿名子类自动导出；`clone()` 在 Cloneable 子类成功、非 Cloneable 抛异常（全部 try/finally unexport）。
- 实现见 `UnicastRemoteObjectTest.java`。
