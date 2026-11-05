# javax.management.loading.PrivateClassLoader — 设计案

## 1. API 功能
`PrivateClassLoader`：纯标记接口（无方法、无字段、不继承任何接口）。实现它的 ClassLoader 被注册为 MBean 时，`MBeanServer` 的 `ClassLoaderRepository` **不**把它加入搜索列表（`loadClass/loadClassWithout/loadClassBefore` 永不咨询它），但仍按名记忆它（`MBeanServer.getClassLoader(ObjectName)` 可取回、`createMBean` 显式指定加载器可用），用于"私有"加载器不参与共享委派的场景。JDK 提供的实现类是 `PrivateMLet`。

## 2. Java 用户典型用法
```java
class MyPrivateLoader extends URLClassLoader implements PrivateClassLoader { ... }
mbs.registerMBean(myPrivateLoader, name);
// repo.loadClass 不再考虑它；但：
ClassLoader cl = mbs.getClassLoader(name);          // 仍可显式取回
mbs.createMBean("com.example.Bean", on, name);       // 仍可显式作为加载器
```

## 3. 参考来源
- Oracle javadoc：`javax.management.loading.PrivateClassLoader`（标记语义说明）。
- OpenJDK `com/sun/jmx/mbeanserver/ClassLoaderRepositorySupport.addClassLoader(ObjectName, ClassLoader)`：`if (!(loader instanceof PrivateClassLoader)) add(...)`——排除逻辑的直接出处；`DefaultMBeanServerInterceptor` 注册 ClassLoader MBean 的调用链。
- OpenJDK `test/jdk/javax/management/loading/MLetCLR/`：私有/公共 MLet 与 CLR 交互回归思路。代码自写。

## 4. 测试用例设计（深化 2026-11）
维度与用例映射（断言 3 → 16，JDK-8 jtreg 全绿）：
- **标记形态**：`getDeclaredMethods().length == 0`、`getMethods().length == 0`（无方法接口不报告 Object 成员——JDK-8 实测）、无字段、不扩展接口。
- **实现者矩阵**：`PrivateMLet` 实现之；普通 `MLet` 不实现（公共/私有对照）；用户自定义 `URLClassLoader implements PrivateClassLoader` 组合合法（无需实现任何方法）。
- **语义闭环**（核心，实测）：公共 URLClassLoader MBean 注册后 `repo.loadClassWithout(systemLoader, NAME)` 由它定义（定义加载器同一性断言），注销后 CNFE；标记同一 URL 加载器注册后同一调用仍 CNFE（排除语义），但 `server.getClassLoader(name)` 仍返回它、直接 `loadClass` 仍可用。
- **易错规范细节**：测试用加载器须同时符合 Standard MBean 命名约定（`XxxMBean` 接口）否则 registerMBean 抛 `NotCompliantMBeanException`——JDK-27 实测踩坑（见版本差异）。

## 版本差异
- JDK-27 仍保留本接口（jimage 三类之一），但 JDK 提供的实现者 `PrivateMLet` 已随 MLet 家族移除；27 侧用用户自定义加载器实证同一语义（JDK-27 侧 DESIGN 见其目录）。
