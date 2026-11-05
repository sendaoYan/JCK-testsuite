# javax.management.loading.ClassLoaderRepository — 设计案

## 1. API 功能
- 单一接口：表示 MBeanServer 的类加载器仓库，无实现类约束。
- 两个方法：`loadClass(String className)` 委派给仓库中第一个能加载该类的 ClassLoader；`loadClassWithout(ClassLoader exclude, String className)` 在排除指定 loader 的集合里加载。
- 两者都可能抛 `ClassNotFoundException`；`loadClassWithout` 的 `exclude` 可为 null。

## 2. Java 用户如何使用
用户通常不直接实例化该接口，而是通过 `MBeanServer.getClassLoaderRepository()` 取得，或在自定义 loader 上实现该接口参与类仓库。
```java
ClassLoaderRepository repo = mbs.getClassLoaderRepository();
Class<?> c = repo.loadClass("com.example.ManagedResource");
```

## 3. 开源代码实际用法调研
- JDK 自用：`javax.management.loading.MLet` / `DefaultLoaderRepository` 相关代码；`MBeanServer.getClassLoaderRepository()` 返回该接口实例。
- `test/jdk/javax/management/loading/LoaderRepositoryTest.java` 覆盖 loadClass / loadClassWithout 的委派与 ClassNotFoundException 路径。
- 说明：接口行为集中在“委派加载”和“未命中抛异常”两点，测试据此设计匿名实现来黑盒验证。

## 4. 测试用例设计
- 白盒：public 接口、非类、方法集合含 `loadClass` 与 `loadClassWithout`。
- 黑盒：匿名实现中 `loadClass` 对已知类返回对应 Class，对未知类抛 `ClassNotFoundException`；验证 `loadClass("java.lang.String")` 正确返回、未知类名抛异常、`loadClassWithout` 排除参数透传。

## 深化（2026-11）
断言 19 → 50，JDK-27 jtreg 全绿。新增维度：
- **精确接口面**：`getMethods().length == 3`；逐方法返回 `Class`、签名 `(String)`/`(ClassLoader,String)`、仅声明 `ClassNotFoundException`、不继承接口。
- **种子语义（真实 server 仓库）**：`newMBeanServer().getClassLoaderRepository()` 的 `loadClass("java.lang.String")==String.class`、应用类（测试嵌套类名）== 本地 Class、未知类 CNFE 消息恰为类名（与 JDK-8 一致）。
- **without/before 语义矩阵**：`loadClassWithout(null,..)`/`loadClassBefore(null,..)` 等价 plain；`loadClassWithout(systemLoader, 应用类)` → CNFE；`loadClassWithout(systemLoader, String)` 成功；`loadClassBefore(systemLoader, 应用类)` → CNFE（搜索在 system 处停止，不咨询其自身）。
- **动态参与闭环**：临时目录放置嵌套类 .class，注册公共 URLClassLoader MBean（bootstrap-only 父）前 `loadClassWithout(sys, NAME)` CNFE → 注册后由该加载器定义（定义加载器同一性 + 跨加载器不同 Class 实例）→ 注销后 CNFE。
- **私有排除**：`PrivateClassLoader` 加载器注册后三入口均不咨询，但 `server.getClassLoader(name)` 可取回、直接 loadClass 可用（27 侧用自定义加载器实证——MLet 家族已移除）。
- **tracked server 路径**：`createMBeanServer` + finally `releaseMBeanServer`，`findMBeanServer(null)` 可见性前后断言。

### 版本差异
- JDK-27 的 `javax.management.loading` 仅剩 ClassLoaderRepository/DefaultLoaderRepository/PrivateClassLoader（jimage 实测）；MLet 家族（MLet/MLetMBean/MLetContent/PrivateMLet）整体移除，故 27 侧闭环由 `URLClassLoader` 子类标准 MBean 驱动（JDK-8 侧还可用 MLet）。接口语义本身两侧一致。
