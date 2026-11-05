# javax.management.loading.PrivateMLet — 设计案

## 1. API 功能
`PrivateMLet extends MLet implements PrivateClassLoader`：带"私有"语义的 MLet——功能与 MLet 完全相同（URL 搜索、库目录、MLET 文件加载、标准 MBean 视图），唯一差别是注册为 MBean 后不进入 `ClassLoaderRepository` 搜索列表。

构造（恰 3 个，均比 MLet 对应形态多 `boolean delegateToCLR`）：
- `PrivateMLet(URL[] urls, boolean delegateToCLR)`
- `PrivateMLet(URL[] urls, ClassLoader parent, boolean delegateToCLR)`
- `PrivateMLet(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory, boolean delegateToCLR)`

## 2. Java 用户典型用法
```java
PrivateMLet priv = new PrivateMLet(new URL[]{libUrl}, true);
mbs.registerMBean(priv, new ObjectName("Services:type=PrivateMLet"));
// repo.loadClass 不会路由到它；但 mbs.getClassLoader(name) 与显式加载器用法不受影响
mbs.createMBean("com.example.Secret", on, privName);
```

## 3. 参考来源
- Oracle javadoc：`javax.management.loading.PrivateMLet`（私有语义说明）。
- OpenJDK `ClassLoaderRepositorySupport.addClassLoader` 的 `instanceof PrivateClassLoader` 排除逻辑；`DefaultMBeanServerInterceptor` 的 ClassLoader MBean 注册链。
- OpenJDK `test/jdk/javax/management/loading/MLetCLR/MLetCommand.java`：私有 MLet 与 CLR 交互思路。代码自写。

## 4. 测试用例设计（深化 2026-11）
维度与用例映射（断言 5 → 30，JDK-8 jtreg 全绿）：
- **构造矩阵**：3 构造逐一（URLs 回读、库目录初始化、父委派 `java.lang.String`/测试类同一 Class、null parent=bootstrap、null factory 合法、boolean 两形态）。
- **结构对照**：`getDeclaredMethods().length == 0`（自身零方法，行为全继承 MLet）；`getConstructors().length == 3`；instanceof MLet/MLetMBean/PrivateClassLoader 三重断言。
- **继承行为**：addURL、set/getLibraryDirectory、loadClass 未知类 CNFE 消息即类名（均在 PrivateMLet 实例上复验）。
- **private 语义闭环**（核心，与公共 MLet 同 URL 对照实测）：公共 MLet 注册 → `repo.loadClassWithout(systemLoader, NAME)` 由其定义；注销 → CNFE；PrivateMLet 注册 → 同一调用仍 CNFE（排除），但 `server.getClassLoader(name) == priv`、`isRegistered` 正常、`priv.loadClass(NAME)` 自定义义（与 classpath 副本不同 Class 实例、常量值相同）、系统类父委派正常；注销簿记清理。
- **易错规范细节**："私有"不影响注册簿记（isRegistered/MBeanInfo/getClassLoader 全正常），只影响 CLR 搜索列表；`repo.loadClassWithout` 的 CNFE 消息恰为类名。

## 版本差异
- JDK-27 已移除 MLet 家族（含本类），本目录仅 JDK-8 存在；27 侧语义由 `PrivateClassLoader` 目录用自定义加载器锁定。
