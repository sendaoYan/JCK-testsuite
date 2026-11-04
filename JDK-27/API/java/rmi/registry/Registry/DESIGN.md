# java.rmi.registry.Registry 行为测试设计

## 1. API 功能
`Registry` 是远程注册表接口，`extends Remote`。
- 常量 `REGISTRY_PORT = 1099`。
- `lookup/bind/unbind/rebind/list` 五个远程方法（均 throws RemoteException 及相应命名异常）。

## 2. Java 用户如何使用
- 服务端 `registry.bind("svc", impl)`；客户端 `registry.lookup("svc")`、`registry.list()`。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/registry/Registry.java`。
- JDK 测试 `test/jdk/java/rmi/registry/readTest/RegistryLookup.java` 调用 `LocateRegistry.getRegistry(...).lookup(...)`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（RegistryTest.testBlackBox）：断言 `REGISTRY_PORT == 1099`；用本地内存实现类走 `bind -> lookup -> list -> rebind 替换 -> unbind -> list 为空` 的组合链路。
白盒部分（testWhiteBox）：接口、extends Remote、REGISTRY_PORT 为 static final、五个方法形态。
