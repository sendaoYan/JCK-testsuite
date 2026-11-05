# javax.management.loading.PrivateClassLoader — 设计案

## 1. API 功能
- 单一标记接口（marker interface），无方法、无字段。
- 语义：标识某个 ClassLoader 不应被放入 MBeanServer 的默认类加载器仓库（即“私有” loader）。

## 2. Java 用户如何使用
用户让自定义 ClassLoader 实现该接口，表示它不参与 `ClassLoaderRepository` 的共享委派。
```java
class PrivateLoader extends ClassLoader implements PrivateClassLoader {}
```

## 3. 开源代码实际用法调研
- JDK `javax.management.loading.MLet` 文档与相关源码将实现该接口的 loader 排除出默认仓库。
- 该接口在开源项目中使用极少，仅作为能力标记，测试聚焦接口结构与可组合性。

## 4. 测试用例设计
- 白盒：public 接口、`getDeclaredMethods().length == 0`、`getDeclaredFields().length == 0`。
- 黑盒：定义 `class PrivateLoader extends ClassLoader implements PrivateClassLoader`，断言实例同时是 `ClassLoader` 与 `PrivateClassLoader`。

## 深化（2026-11）
断言 12 → 18，JDK-27 jtreg 全绿。新增维度（语义闭环，全部实测）：
- **排除语义对照**：临时目录放置嵌套类 .class；公共 URLClassLoader MBean（bootstrap-only 父）注册后 `repo.loadClassWithout(systemLoader, NAME)` 由其定义（counterfactual 成立）；换成实现本标记接口的同形加载器注册后同一调用仍 CNFE（排除语义），且 `loadClass`/`loadClassBefore(priv, ..)` 亦不咨询。
- **按名可达性**：`server.getClassLoader(privateName) == priv`、`isRegistered` 正常、直接 `loadClass(NAME)` 定义加载器即自身；注销簿记清理。
- **版本事实锁定**：`Class.forName("javax.management.loading.MLet")` 在 JDK-27 抛 CNFE——MLet 家族（含原 JDK 提供实现者 PrivateMLet）已从本包移除，27 侧用自定义加载器实证同一标记语义。
- **易错细节**：测试用加载器须符合 Standard MBean 命名约定（`Xxx` + `XxxMBean`），否则 registerMBean 抛 `NotCompliantMBeanException`（27 实测踩坑：实现他人的 `XxxMBean` 接口不合规范）。

### 版本差异
- JDK-8 侧另有 MLet/PrivateMLet 目录以 JDK 自带实现者复验同一语义；JDK-27 仅本目录，接口本身两侧一致。
