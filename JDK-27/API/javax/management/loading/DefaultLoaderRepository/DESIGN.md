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

## 深化（2026-11）
断言 19 → 31，JDK-27 jtreg 全绿。新增维度：
- **空聚合负例**（先于任何 server 创建执行）：无 tracked server 时 `loadClass("java.lang.String")` 与 `loadClassWithout(null, "java.lang.String")` 均抛 CNFE 且消息恰为类名——聚合源仅限 `findMBeanServer(null)`（`newMBeanServer()` 的 server 不可见）；与 JDK-8 实测一致。
- **精确形态**：直接 Object 子类、不实现接口、唯一公开无参构造可实例化、自身公开方法恰 2 个且均 static、返回 Class、仅声明 CNFE。
- **聚合闭环**：tracked server 内注册公共 URLClassLoader MBean（bootstrap-only 父）→ `loadClassWithout(systemLoader, 嵌套类名)` 由该加载器定义（定义加载器同一性 + 跨加载器不同 Class 实例）；注销后再次 CNFE；finally `releaseMBeanServer` + 临时目录清理；未知类 CNFE 消息恰为类名。
- **易错细节**：与同为废弃的 `javax.management.DefaultLoaderRepository` 同名并存（27 仍存在），需显式 import `javax.management.loading.DefaultLoaderRepository`。

### 版本差异
- 行为与 JDK-8 完全一致（@Deprecated、空聚合负例、CNFE 消息、聚合语义逐项双侧复测通过）。
