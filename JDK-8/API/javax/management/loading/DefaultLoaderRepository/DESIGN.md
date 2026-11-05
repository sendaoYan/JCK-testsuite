# javax.management.loading.DefaultLoaderRepository — 设计案

## 1. API 功能
`DefaultLoaderRepository`（`@Deprecated`）：静态工具类，遍历 `MBeanServerFactory.findMBeanServer(null)`（即 `createMBeanServer` 创建、未 release 的 server）的 ClassLoaderRepository 做委派加载。新代码应改用 `MBeanServer.getClassLoaderRepository()`。

方法（恰 2 个自身公开静态方法 + 1 个公开无参构造，均抛 `ClassNotFoundException`）：
- `static loadClass(String className)` == `load(null, className)`
- `static loadClassWithout(ClassLoader without, String className)`

实现要点（OpenJDK 源）：逐 server 调 `clr.loadClassWithout(without, name)`，某 server 成功即返回；全部失败抛 `ClassNotFoundException(className)`（消息恰为类名）。**没有任何 tracked server 时（空列表）连 `java.lang.String` 也抛 CNFE**——聚合源仅限 tracked server，`newMBeanServer()` 创建的 server 对其不可见。

## 2. Java 用户典型用法
```java
// 旧代码（已废弃）：
Class<?> c = DefaultLoaderRepository.loadClass("com.example.Bean");
Class<?> d = DefaultLoaderRepository.loadClassWithout(excluded, "com.example.Bean");
// 新代码：
Class<?> e = mbs.getClassLoaderRepository().loadClass("com.example.Bean");
```

## 3. 参考来源
- Oracle javadoc：`javax.management.loading.DefaultLoaderRepository`（废弃说明与替代方案）。
- OpenJDK `jdk/src/share/classes/javax/management/loading/DefaultLoaderRepository.java`（jdk8u-dev）：`load()` 的 findMBeanServer 聚合与 `new ClassNotFoundException(className)`。
- OpenJDK `test/jdk/javax/management/loading/` 相关回归思路。代码自写。

## 4. 测试用例设计（深化 2026-11）
维度与用例映射（断言 5 → 28，JDK-8 jtreg 全绿）：
- **废弃注解**：类级 `@Deprecated` 存在（javap -v 双侧确认 Deprecated: true）。
- **形态**：直接 Object 子类、不实现接口、唯一公开无参构造可实例化、自身方法恰 2 个且均 static、返回 Class、仅声明 CNFE。
- **空聚合负例**（核心，先于任何 server 创建执行）：无 tracked server 时 `loadClass("java.lang.String")` 与 `loadClassWithout(null, "java.lang.String")` 均抛 CNFE 且消息即类名（双侧实测一致；防御分支：若环境已有 tracked server 则跳过该断言）。
- **tracked 聚合矩阵**：`createMBeanServer` 后静态方法经其仓库解析 String/应用类；未知类 CNFE 消息恰为类名；`loadClassWithout(null,..)` == plain。
- **聚合闭环**（核心）：tracked server 内注册公共 URLClassLoader MBean（bootstrap-only 父）→ `loadClassWithout(systemLoader, NAME)` 由该注册加载器定义（跨加载器不同 Class 实例）；注销后再次 CNFE；finally `releaseMBeanServer` + 临时目录清理。
- **易错规范细节**：本类与同为废弃的 `javax.management.DefaultLoaderRepository`（旧位置副本，两侧均仍存在）同名——import 必须显式写 `javax.management.loading.DefaultLoaderRepository`，否则与 `javax.management.*` 通配 import 产生歧义编译错（JDK-8 实测踩坑）。

## 版本差异
- JDK-27 行为完全一致（@Deprecated、空聚合负例、CNFE 消息、聚合语义逐项复测通过）；两包同名副本在 27 依然并存。
