# javax.management.DefaultLoaderRepository 黑盒行为测试设计

## 1. API 功能
- `@Deprecated` 类。
- 静态方法：`loadClass(String)`、`loadClassWithout(ClassLoader, String)`。

## 2. Java 用户如何使用
兼容旧 JMX 的类加载入口：遍历所有被跟踪的 MBeanServer 的 ClassLoaderRepository 加载类，找不到抛 ClassNotFoundException。
```java
Class<?> c = DefaultLoaderRepository.loadClass("com.example.Foo");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/loading/SystemClassLoaderTest.java`、`ArrayClassTest.java`：类加载行为。
- 实现权威：`src/java.management/share/classes/javax/management/loading/DefaultLoaderRepository.java`（内部走 `MBeanServerFactory.findMBeanServer(null)`）。

## 4. 测试用例设计 + 自动化实现
- 黑盒：先 `MBeanServerFactory.createMBeanServer` 创建被跟踪服务器，loadClass 能解析 java.lang.String/ArrayList，loadClassWithout(null,...) 能解析 Integer；finally 释放服务器；缺失类抛 ClassNotFoundException。
- 白盒：public 类、@Deprecated、静态 loadClass/loadClassWithout。
