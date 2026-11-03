# java.rmi.server.RMIClassLoaderSpi 行为测试设计

## 1. API 功能
- 抽象类，公开无参构造器。
- 抽象方法：`loadClass(String codebase, String name, ClassLoader)`、`loadProxyClass(String, String[], ClassLoader)`、`getClassLoader(String)`、`getClassAnnotation(Class<?>)`。
- 是 `RMIClassLoader` 的服务提供者接口。

## 2. Java 用户如何使用
- 实现子类覆盖四个抽象方法，通过 `java.rmi.server.RMIClassLoaderSpi` 服务配置注册。
- `RMIClassLoader.getDefaultProviderInstance()` 返回默认实现。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/server/RMIClassLoader/spi/TestProvider.java`：自定义 SPI 提供者覆盖四个抽象方法。
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RMIClassLoaderSpi.java`：定义 SPI 契约。
- JDK 内部 `RMIClassLoader$1` 为默认 provider 实现。

## 4. 测试用例设计 + 自动化实现
- 白盒：public abstract、父类 Object、public 无参构造器。
- 黑盒：匿名子类实现四方法；`loadClass` 委托；`loadProxyClass`/`getClassLoader`/`getClassAnnotation` 哨兵返回；默认 provider 是 RMIClassLoaderSpi 实例。
- 实现见 `RMIClassLoaderSpiTest.java`。
