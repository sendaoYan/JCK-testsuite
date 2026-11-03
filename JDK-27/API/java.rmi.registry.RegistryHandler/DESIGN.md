# java.rmi.registry.RegistryHandler 行为测试设计

## 1. API 功能
`RegistryHandler` 是 RMI 运行时的历史内部接口，`@Deprecated`，不应被应用代码使用。
- `Registry registryStub(String host, int port)`、`Registry registryImpl(int port)`。

## 2. Java 用户如何使用
- 仅历史兼容；现代 RMI 不再使用该接口获取注册表 stub/实现。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/registry/RegistryHandler.java`：注释明确「自 Java 2 平台 1.2 起 RMI 不再使用」。
- 无 JDK `test/jdk` 直接用例。

## 4. 测试用例设计 + 自动化实现
黑盒部分（RegistryHandlerTest.testBlackBox）：本地实现类可实例化、`instanceof RegistryHandler`、两个方法可调用返回 null。
白盒部分（testWhiteBox）：接口、无父接口、@Deprecated、方法 registryStub/registryImpl 形态。
