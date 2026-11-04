# javax.management.loading.DefaultLoaderRepository — 设计案

## 1. API 功能
- 已废弃（@Deprecated）的类，提供静态方法：`loadClass(String)` 与 `loadClassWithout(ClassLoader, String)`。
- 语义：在当前所有已注册 MBeanServer 的 ClassLoaderRepository 上执行委派加载，方法为 static、不可实例化（构造器非 public 亦不影响调用）。

## 2. Java 用户如何使用
旧代码通过静态方法从全局默认仓库加载类；新代码应改用 `MBeanServer.getClassLoaderRepository()`。
```java
Class<?> c = DefaultLoaderRepository.loadClass("com.example.Bean"); // 已废弃
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/loading/DefaultLoaderRepositoryTest.java` 验证静态方法委派与异常。
- `javax.management.loading.MLet` 文档及历史代码曾依赖默认仓库；当前实现内部遍历 `MBeanServerFactory` 已注册 server。
- 说明：本类仅剩兼容价值，测试聚焦静态方法与 ClassNotFoundException 传播。

## 4. 测试用例设计
- 白盒：非接口、public、带 `@Deprecated`、方法含 `loadClass`/`loadClassWithout`，且为 static。
- 黑盒：创建并释放一个 MBeanServer，断言 `loadClass("java.lang.String") == String.class`、`loadClassWithout(null, "java.lang.Integer") == Integer.class`、未知类名抛 `ClassNotFoundException`。
