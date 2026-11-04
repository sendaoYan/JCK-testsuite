# java.util.ServiceLoader 测试设计

## 1. API 功能
- 工厂：`load(Class)`、`load(Class, ClassLoader)`、`loadInstalled`、`load(ModuleLayer, Class)`。
- 迭代：`iterator`、`stream`、`findFirst`、`reload`、`toString`。

## 2. Java 用户如何使用
- 加载服务：`ServiceLoader.load(Svc.class).iterator()`。
- 取首个实现：`ServiceLoader.load(Svc.class).findFirst()`。
- 流式处理：`ServiceLoader.load(Svc.class).stream()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/Console.java`：`ServiceLoader.load(...)` 加载实现。
- JDK 源码 `java/net/spi/InetAddressResolverProvider.java`：SPI 接口。
- JDK 源码 `java/net/URL.java`：`ServiceLoader.load` 查找 handler。
- 广泛用法：SPI 插件机制、日志框架、JDBC 驱动。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、实现 Iterable、关键方法存在。
- 黑盒（未注册提供者的服务，确定性为空）：
  - iterator 无元素、findFirst 为空 Optional、stream 计数 0、reload 后仍为空。
