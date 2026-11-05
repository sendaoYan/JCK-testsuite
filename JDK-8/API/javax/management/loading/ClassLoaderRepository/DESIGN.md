# javax.management.loading.ClassLoaderRepository — 设计案

## 1. API 功能
`ClassLoaderRepository`：MBeanServer 的类加载器仓库接口。真实实现由 `MBeanServer.getClassLoaderRepository()` 返回（内部 `SecureClassLoaderRepository` 包装 `ClassLoaderRepositorySupport`），搜索顺序语义由 `ClassLoaderRepositorySupport.loadClass` 固化。

方法（恰 3 个，均抛 `ClassNotFoundException`，失败时消息恰为类名）：
- `loadClass(String)`：按列表顺序取第一个能加载的加载器；条目为 null 表示 bootstrap（`Class.forName(name, false, null)`）。
- `loadClassWithout(ClassLoader exclude, String name)`：跳过 exclude（null 等价于 loadClass）。
- `loadClassBefore(ClassLoader stop, String name)`：遇到 stop 即**停止**（只咨询列表中 stop 之前的加载器，不含 stop；null 等价于 loadClass）。

种子与动态性：server 创建时注入 JMX 类自身加载器与系统加载器；注册 ClassLoader 型 MBean 追加到列表尾部（实现 PrivateClassLoader 的除外），注销即移除。

## 2. Java 用户典型用法
```java
MBeanServer mbs = MBeanServerFactory.newMBeanServer();
ClassLoaderRepository repo = mbs.getClassLoaderRepository();
Class<?> c = repo.loadClass("com.example.Bean");           // 全仓库顺序搜索
Class<?> d = repo.loadClassWithout(exclude, "com.example.Bean");
Class<?> e = repo.loadClassBefore(myLoader, "com.example.Bean"); // 只用 myLoader 之前注册的
```

## 3. 参考来源
- Oracle javadoc：`javax.management.loading.ClassLoaderRepository`（三方法语义与异常）。
- OpenJDK `com/sun/jmx/mbeanserver/ClassLoaderRepositorySupport.java`（jdk8u-dev）：列表顺序、null-without/null-stop 降级、`cl == stop → break`、PrivateClassLoader 排除、CNFE 消息即类名。
- OpenJDK `com/sun/jmx/mbeanserver/JmxMBeanServer.java`：仓库种子（JMX 加载器 + 系统加载器）。
- OpenJDK `test/jdk/javax/management/loading/`（SystemClassLoaderTest 等）回归思路。代码自写。

## 4. 测试用例设计（深化 2026-11）
维度与用例映射（断言 6 → 44，JDK-8 jtreg 全绿）：
- **精确接口面**：3 方法、返回 Class、签名 `(String)`/`(ClassLoader,String)`、仅声明 ClassNotFoundException、不继承接口。
- **种子语义**：`loadClass("java.lang.String") == String.class`；`loadClass(测试嵌套类名) == 本地 Class`（经种子的系统加载器）；未知类 CNFE 消息恰为类名。
- **without/before 语义矩阵**（核心）：`loadClassWithout(null,..)` == plain；`loadClassWithout(sys, 应用类)` → CNFE（只有系统能见）；`loadClassWithout(sys, String)` 成功；`loadClassBefore(sys, String)` 成功（bootstrap 在前）；`loadClassBefore(sys, 应用类)` → CNFE（搜索在 sys 处停止、不咨询 sys 自身）；`loadClassBefore(null,..)` == plain。
- **动态参与闭环**（核心）：临时目录放嵌套类 .class；注册公共 URLClassLoader MBean 前 `loadClassWithout(sys, NAME)` CNFE → 注册后解析且定义加载器 == 注册的加载器（跨加载器不同 Class 实例）且 `plain loadClass` 仍偏系统加载器 → 注销后 CNFE。
- **私有排除**：注册 `PrivateClassLoader` 加载器后三入口均不咨询（CNFE），`server.getClassLoader(name)` 仍可取回、直接 loadClass 可用。
- **createMBeanServer 路径**：tracked server 的仓库同语义（String/应用类/CNFE），`findMBeanServer(null)` 包含之，`releaseMBeanServer` 后移除。
- **易错规范细节**：CNFE 消息恰为类名（两侧一致）；测试加载器需符合 Standard MBean 命名约定；`loadClassBefore` 是"含 stop 之前的、不含 stop"而非"不含 stop 之后的全部列表仍搜索"。

## 版本差异
- JDK-27 接口面与语义完全一致（同一实现源码演化），两侧各自实测通过；27 侧因无 MLet，闭环用 `URLClassLoader` 子类 MBean 驱动。
