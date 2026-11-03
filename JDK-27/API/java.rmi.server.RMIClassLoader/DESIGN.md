# java.rmi.server.RMIClassLoader 行为测试设计

## 1. API 功能
- 加载：`loadClass(String name)`、`loadClass(String codebase, String name)`、`loadClass(String codebase, String name, ClassLoader)`、`loadClass(URL, String)`、`loadProxyClass(...)`。
- 查询：`getClassLoader(String codebase)`、`getClassAnnotation(Class<?>)`、`getSecurityContext(ClassLoader)`（已弃用）、`getDefaultProviderInstance()`。
- 构造器私有，全部静态方法。

## 2. Java 用户如何使用
- `RMIClassLoader.loadClass("java.lang.String")` 从默认 codebase 加载；`getClassAnnotation(cls)` 取类来源注解。
- `getDefaultProviderInstance()` 取默认 `RMIClassLoaderSpi` 提供者。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/server/RMIClassLoader/getClassAnnotation/NullClass.java`：`getClassAnnotation(null)` 抛 NullPointerException。
- JDK 测试 `test/jdk/java/rmi/server/RMIClassLoader/spi/DefaultProperty.java`、`InvalidProperty.java`：验证 provider 属性解析。
- JDK 测试 `test/jdk/java/rmi/registry/classPathCodebase/ClassPathCodebase.java`：codebase 加载场景。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、具体类、单私有构造器、父类 Object。
- 黑盒：`loadClass` 三个重载加载 String；bootstrap 类注解为 null；系统类加载器安全上下文为 null；默认 provider 非 null 且为 RMIClassLoaderSpi 单例；`getClassLoader(null)` 返回可用加载器。
- 实现见 `RMIClassLoaderTest.java`。
