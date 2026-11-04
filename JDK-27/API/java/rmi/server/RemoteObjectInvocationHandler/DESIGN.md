# java.rmi.server.RemoteObjectInvocationHandler 行为测试设计

## 1. API 功能
- 类，`extends RemoteObject implements InvocationHandler`；构造器 `RemoteObjectInvocationHandler(RemoteRef ref)`（ref 为 null 抛 NPE）。
- 方法：`invoke(Object proxy, Method, Object[])`。
- 语义：Object 方法（hashCode/equals/toString）本地处理；远程方法委托 `ref.invoke`；proxy 非 Proxy/非 Remote 时抛 IllegalArgumentException。

## 2. Java 用户如何使用
- 用动态代理替代预生成 stub：`(Remote) Proxy.newProxyInstance(loader, new Class[]{RemoteIface.class}, new RemoteObjectInvocationHandler(ref))`。
- 一般由 `UnicastRemoteObject.exportObject` 内部使用。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RemoteObjectInvocationHandler.java`：`invokeObjectMethod`/`invokeRemoteMethod` 分派。
- JDK 测试 `test/jdk/java/rmi/server/UnicastRemoteObject/useDynamicProxies/UseDynamicProxies.java`：`exportObject` 后校验 `Proxy.getInvocationHandler` 是 RemoteObjectInvocationHandler。
- JDK 测试 `test/jdk/java/rmi/server/RemoteObject/toStub/ToStub.java`：proxy stub 识别。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、父类 RemoteObject、implements InvocationHandler。
- 黑盒：getRef；null ref 抛 NPE；proxy 的 toString/hashCode/equals（同 handler 等/异 handler 不等）；远程方法委托 `ref.invoke`；非 proxy 调 invoke 抛 IllegalArgumentException。
- 实现见 `RemoteObjectInvocationHandlerTest.java`。
