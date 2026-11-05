# javax.management.loading.MLet — 设计案

## 1. API 功能
`MLet extends URLClassLoader implements MLetMBean, MBeanRegistration, Externalizable`：JMX 的 "managed applet" 加载器，既是 URLClassLoader（从 URL 搜索路径加载类与资源），又是可直接注册进 MBeanServer 的标准 MBean（接口 `MLetMBean`），并实现 `MBeanRegistration` 生命周期与 `getMBeansFromURL`（解析 MLET 标签文本文件批量实例化 MBean）。

分组：
- **构造（7 个）**：`()`、`(URL[])`、`(URL[], ClassLoader)`、`(URL[], ClassLoader, URLStreamHandlerFactory)`，以及带 `boolean delegateToCLR` 的三种变体（非 boolean 形态统一委托 `init(true)`，即默认委托 CLR）。
- **URL 管理**：`addURL(URL)`、`addURL(String)`（均去重：已在列表则忽略）、`getURLs()`。
- **类/资源加载**：继承 `loadClass(String)`（父优先双亲委派）、`loadClass(String, ClassLoaderRepository)`（父与自身 URL 失败后走 CLR 的 `loadClassBefore(this, name)`）；`getResource/getResourceAsStream/getResources` 按 URLClassLoader 语义。
- **MBean 视图**：属性 `LibraryDirectory`（String，读写）、`URLs`（URL[]，只读）；`get/setLibraryDirectory`；操作 `getMBeansFromURL(URL)/(String)`。
- **生命周期**：`preRegister`（name 为 null 时返回 `<defaultDomain>:type=MLet`；server 为 null 时 NPE）、`postRegister/preDeregister/postDeregister`（空实现）。
- **外部化**：声明 `Externalizable` 但 `writeExternal/readExternal` 一律抛 `UnsupportedOperationException`。

## 2. Java 用户典型用法
```java
MBeanServer mbs = MBeanServerFactory.createMBeanServer();
MLet mlet = new MLet(new URL[]{new URL("file:/opt/beans/")});
ObjectName name = new ObjectName("Services:type=MLet");
mbs.registerMBean(mlet, name);                       // 注册后进入 ClassLoaderRepository
mbs.createMBean("com.example.Bean", objectName, name); // 用该 MLet 作为加载器实例化
Set<Object> results = mlet.getMBeansFromURL("file:/opt/beans/mbeans.mlet");
mlet.addURL("http://repo.example.com/lib/");
Class<?> c = mlet.loadClass("com.example.Impl", mbs.getClassLoaderRepository());
```

## 3. 参考来源
- Oracle javadoc：`javax.management.loading.MLet`（构造、addURL 去重语义、getMBeansFromURL 异常表、Externalizable 拒绝说明）。
- OpenJDK `jdk/src/share/classes/javax/management/loading/MLet.java`（jdk8u-dev）：`init()` 的 `libraryDirectory` 初始化（`jmx.mlet.library.dir` 或 tmpdir）、`addURL` 去重与相对 URL 包装、`getMBeansFromURL` 的 server-null 检查顺序（URL 重载先查 null、String 重载先查注册态）、`findClass(name, clr)` 对 `clr.loadClassBefore` 的回退。
- OpenJDK `test/jdk/javax/management/loading/`（GetMBeansFromURLTest、DocumentRootTest、LibraryLoader、MLetCLR）：MLET 文件解析与 CLR 委托回归思路。代码自写，未拷贝。

## 4. 测试用例设计（深化 2026-11）
维度与用例映射（断言 10 → 80，jtreg 双侧本侧 JDK-8 全绿）：
- **构造矩阵**：7 种构造逐一 getURLs 回读；无参空表；`(URL[],ClassLoader)` 对 `java.lang.String`/测试类返回父加载器同一 Class（委派链单例）。
- **addURL 去重 quirk**：同 URL 二次 `addURL(URL)`/`addURL(String)` 均被忽略（与裸 URLClassLoader 不同）；相对 spec `addURL("relative/path")` → `ServiceNotFoundException("The specified URL is malformed")`（new URL 无上下文 → MalformedURLException 包装）。
- **负例异常表**（精确消息）：`loadClass("no.such.Clazz")` → CNFE 消息即类名；`addURL(relative)` → SNF 精确消息；`writeExternal/readExternal` → `UnsupportedOperationException("MLet.writeExternal"/"MLet.readExternal")`；未注册时 `getMBeansFromURL(String)`/`(URL)` → `IllegalStateException("This MLet MBean is not registered with an MBeanServer.")`，但 `(URL)null` 先做 null 检查 → `ServiceNotFoundException("The specified URL is null")`；注册后不存在文件 → SNF 消息以 `Problems while parsing URL [` 开头、含 URL 串、cause 为 `FileNotFoundException`；未知协议 `bogusproto://` → SNF cause `MalformedURLException("unknown protocol: bogusproto")`。
- **资源语义**：getResource/getResourceAsStream（父可见资源非空 + class 文件魔数 CA FE BA BE）、getResources 永不返回 null（未知资源为空枚举）。
- **LibraryDirectory**：初始非空；set/读往返；set(null) 后 get 为 null。
- **生命周期**：preRegister 原样返回给定名；preRegister(null server, null name) → NPE；registerMBean(mlet, null) 得 `DefaultDomain:type=MLet`；MBeanInfo 属性恰为 `LibraryDirectory`(rw)/`URLs`(ro, `[Ljava.net.URL;`)；经 server set/getAttribute 往返；注销后 isRegistered false。
- **CLR 参与闭环**（核心）：把本测试嵌套类 .class 复制进临时目录，MLet(目录 URL, bootstrap-only 父) 注册前 `repo.loadClassWithout(systemLoader, NAME)` CNFE → 注册后解析且定义加载器 == 该 MLet（跨加载器不同 Class 实例、常量值相同）→ 注销后再次 CNFE。
- **loadClass(name, CLR) 委托**：空 URL MLet + 假 CLR（loadClassBefore 经另一 PrivateMLet 定义）证明父与 URL 失败后仅走 `loadClassBefore(this, name)`；系统类父优先不问 CLR；未知类 CNFE 消息即类名。
- **易错规范细节**：未注册实例调用 getMBeansFromURL(String) 抛 ISE 而非 SNF（状态检查在参数检查之前）；(URL) 重载把 null 检查放在最前；addURL 双形态均去重；getMBeansFromURL 的 SNF cause 链（EnvHelp.initCause）保真。

## 版本差异
- JDK-27 的 `javax.management.loading` 已整体移除 MLet 家族（jimage 仅剩 ClassLoaderRepository/DefaultLoaderRepository/PrivateClassLoader），本目录仅 JDK-8 存在，测试锁定 JDK-8 行为。
