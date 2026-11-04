# java.net.URLClassLoader 测试设计案（JDK-8）

## 1. API 功能

- 从 URL 数组（目录或 jar）加载类与资源的 ClassLoader；实现 Closeable。
- 构造器：`(URL[])`、`(URL[], ClassLoader)`、`(URL[], ClassLoader, URLStreamHandlerFactory)`——JDK-8 共 3 个 public。
- 静态工厂：`newInstance(URL[])`、`newInstance(URL[], ClassLoader)`。
- 方法：`getURLs/close/findResource/findResources/getResourceAsStream`；protected `addURL`。

## 2. 使用方式

`new URLClassLoader(new URL[]{jarOrDir.toURI().toURL()}, parent)`；`close()` 释放 jar 句柄；
资源经 `getResource(s)/getResourceAsStream` 查找；子类覆写 `addURL` 可动态扩展搜索路径。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/URLClassLoader.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/URLClassLoader/`（CloseTest、FindResources、AddURL）。
- 探针：/tmp/probe-B/PCoder.java（双侧运行）。

## 4. 测试用例设计（URLClassLoaderTest.java，100 断言）

类字节来源：**内嵌 395 字节的 Java-8 classfile（major 52）** `public class Mini implements Serializable { CONST=42; val()=7; tag()="mini-v1" }`，
运行时写成 mini.jar（ZipOutputStream）与目录两种形态——无需外部 javac，结果确定。

| 测试点 | 方法 | 断言要点 |
|---|---|---|
| 白盒 | testWhiteBox | 3 个 public 构造器；Closeable；非 final；addURL protected |
| 构造矩阵 | testConstructorMatrix | null urls → NPE（4 个入口全部）；空 urls 可用；1 参默认 parent=system CL；parent null → Object 类加载器为 null（bootstrap） |
| getURLs | testGetURLs | 构造顺序保留；**返回副本**（改元素不影响）；close 后仍返回；每次新数组 |
| close 语义 | testCloseSemantics | 幂等（3 次）；已加载类 close 后仍可用（方法调用成功）；缓存 Class 恒等；新类 CNFE；getResource/getResourceAsStream/findResource close 后 null |
| jar 加载 | testJarClassLoading | loadClass/静态方法/实例方法(newInstance)/常量字段/Serializable 接口/superclass/defining loader 身份/重复 loadClass 缓存；getResource protocol=jar；资源内容精确；未知资源/类 null/CNFE |
| 目录加载 | testDirectoryClassLoading | 同 class 字节目录形态加载调用成功；file: 协议资源；同字节不同 loader → 不同 Class |
| 父委派 | testParentDelegation | String/ArrayList 经 system parent 身份解析；findResource 只查自身 urls；getResource/getResources 聚合父链（java/lang/Object.class 可见） |
| addURL | testAddURL | protected；新 URL 追加成功；**重复 URL 被忽略（去重）**；**null URL 静默忽略**（探针实测） |
| 多 jar 优先级 | testMultipleJarsFirstWins | 同名资源在两个 jar：顺序决定 getResourceAsStream 命中体；getResources 枚举两个 |
| loader 身份 | testLoaderIdentity | 相同字节不同 loader → Class 不同、实例不互认 isInstance、binary name 相同、各自缓存 |
| 系统资源 | testSystemResourceDelegation | java/net/URL.class 经父链可见；`Class.forName("[Ljava.lang.Object;", false, ucl)` 数组类 |

## 易错规范细节

- **addURL 去重并忽略 null**（JDK-8 实测；不要断言"追加不检查"）。
- close 后已加载类仍可正常**使用**（ JVM 已链接），只有**新**加载/资源查找失败。
- 数组 binary name 需 `Class.forName(name, false, loader)`，`loadClass("[L...;")` 会 CNFE。
- jar 与目录同字节同 parent 仍是不同 Class（loader 身份决定）。

## 版本差异（JDK-8 vs JDK-27）

- JDK-27 有 5 个 public 构造器（多 `(String name, URL[], CL)` 与带 factory 变体），name 经 `getName()` 暴露；JDK-8 无。
