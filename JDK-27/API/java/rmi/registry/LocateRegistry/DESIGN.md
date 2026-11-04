# java.rmi.registry.LocateRegistry 行为测试设计

## 1. API 功能
`LocateRegistry` 是获取/创建 RMI 注册表的工厂（final、私有构造器）。
- `getRegistry()` 及 4 个重载：返回注册表 stub，**不建立连接**，即使没有注册表运行也成功。
- `createRegistry(int)` 及重载：真正导出并监听端口（本测试不调用，避免绑端口）。

## 2. Java 用户如何使用
- `Registry r = LocateRegistry.getRegistry("host", 1099);` 获取 stub 后再 `r.lookup(...)`。
- 注意 `getRegistry` 成功后，后续方法调用才可能因无注册表而抛异常。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/registry/readTest/RegistryLookup.java`、`serialFilter/RegistryFilterTest.java`、`nonLocalRegistry/NonLocalRegistryTest.java` 使用 `LocateRegistry.getRegistry`。
- 类定义 `src/java.rmi/share/classes/java/rmi/registry/LocateRegistry.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（LocateRegistryTest.testBlackBox）：各 `getRegistry` 重载均返回非 null 的 Registry stub；port=0 被修正为默认端口；不调用任何远程方法。
白盒部分（testWhiteBox）：final、私有构造器、public static 方法 getRegistry/createRegistry。
