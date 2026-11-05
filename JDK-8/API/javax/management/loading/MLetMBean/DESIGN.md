# javax.management.loading.MLetMBean — 设计案

## 1. API 功能
`MLetMBean`：MLet 的标准 MBean 管理接口。实现类（`MLet` 及其子类 `PrivateMLet`）注册进 MBeanServer 后，管理客户端经本接口操作加载器的 URL、库目录与 MLET 文件加载。

方法集（javap -public 实测恰 10 个，全 public abstract，不继承任何接口）：
- `getMBeansFromURL(String)` / `getMBeansFromURL(URL)`，均抛 `ServiceNotFoundException`
- `addURL(URL)` / `addURL(String)`（后者抛 `ServiceNotFoundException`）
- `getURLs()`
- `getResource(String)` / `getResourceAsStream(String)` / `getResources(String)`（后者抛 `IOException`）
- `getLibraryDirectory()` / `setLibraryDirectory(String)`

## 2. Java 用户典型用法
```java
MLetMBean view = (MLetMBean) mbs.getAttribute(name, ...) // 或 JMX.newMBeanProxy(mbs, name, MLetMBean.class)
view.addURL("http://repo/lib/");
URL[] urls = view.getURLs();
view.setLibraryDirectory("/var/lib/mlet");
Set<Object> created = view.getMBeansFromURL("http://host/mbeans.mlet");
```

## 3. 参考来源
- Oracle javadoc：`javax.management.loading.MLetMBean`（方法表与异常声明）。
- `javap -public javax.management.loading.MLetMBean`（JDK-8 实测方法面）。
- OpenJDK `MLet.java` 中各方法的实现行为（异常表）。

## 4. 测试用例设计（深化 2026-11）
维度与用例映射（断言 6 → 35，JDK-8 jtreg 全绿）：
- **精确接口面**：`getMethods().length == 10`；逐方法 public+abstract；声明异常仅允许 `ServiceNotFoundException`/`IOException`；无字段、不扩展任何接口。
- **is-a**：`MLet implements MLetMBean`（assignable）。
- **可调用性（接口视图逐方法）**：`(MLetMBean) mlet` 引用上 `addURL(URL)`/`addURL(String)` 后 `getURLs()` 精确回读；`getResource`/`getResourceAsStream`/`getResources` 非 null；`set/getLibraryDirectory` 往返。
- **接口视图下的异常表**：未注册 `getMBeansFromURL((URL)null)` → `ServiceNotFoundException("The specified URL is null")`；未注册 `getMBeansFromURL("file:...")` → `IllegalStateException`（注册态检查）；注册后 `getMBeansFromURL(URL 不存在文件)` → SNF 且消息含 URL。
- **易错规范细节**：两个重载族（getMBeansFromURL/addURL 各 2 个）以 name+参数数区分；接口无常量/默认方法。

## 版本差异
- JDK-27 已移除 MLet 家族，本目录仅 JDK-8 存在。
