# URLClassLoader 设计文档

## 一、API 功能

从一组 URL(目录或 jar)加载类与资源的 ClassLoader，支持 close 释放资源。

## 二、Java 用户如何使用

用户 new URLClassLoader(urls, parent) 构造并 loadClass()，从远程或本地 jar 动态加载类。

## 三、开源代码实际用法调研

应用服务器、插件系统、构建工具(Maven/Gradle 隔离类加载)广泛使用 URLClassLoader。

## 四、测试用例设计

用临时目录中的类字节加载(或用自身 URL)验证 loadClass；getURLs 返回构造时数组副本；close 后资源关闭；继承 ClassLoader。

## 缺口分析与深化（批次 B，2026-11-05）

原版 15 断言。深化后 104 断言，与 JDK-8 侧同构（内嵌 395 字节 major-52 classfile `Mini`，
运行时造 mini.jar + 目录双形态），新增维度：

| 深化点 | 方法 | 断言要点 |
|---|---|---|
| 白盒（JDK-27 特有） | testWhiteBox | **5 个 public 构造器**（含 `(String name, URL[], CL)` 与 `(String, URL[], CL, Factory)`）；name 参数经 `getName()` 暴露（"my-loader" 精确断言） |
| 构造矩阵 | testConstructorMatrix | null urls → NPE（4 入口）；空 urls；默认 parent=system CL；parent null → bootstrap；newInstance(null parent) |
| getURLs | testGetURLs | 顺序保留、返回副本、close 后仍返回、每次新数组 |
| close 语义 | testCloseSemantics | 幂等 3 次；已加载类仍可用；新类 CNFE；资源查找 null |
| jar/目录加载 | testJarClassLoading / testDirectoryClassLoading | loadClass/方法/字段/接口/缓存/资源内容/协议 jar 与 file |
| 父委派 | testParentDelegation | 身份解析、findResource 只查自身、getResources 聚合父链 |
| addURL | testAddURL | protected；追加成功；重复 URL 去重；null 忽略（与 JDK-8 一致，探针实测） |
| 多 jar 优先级 | testMultipleJarsFirstWins | 顺序决定命中体；getResources 枚举两个 |
| loader 身份 | testLoaderIdentity | 同字节不同 loader → 不同 Class、isInstance false |
| 系统资源 | testSystemResourceDelegation | 模块化下 java/net/URL.class 经父链可见；Class.forName 数组类 |

## 版本差异（JDK-8 vs JDK-27）

- 构造器数量 3 vs 5（named-loader 变体 JDK-9+）。
- null urls NPE 带消息（JDK-27 "Cannot read the array length..."）vs 无消息（JDK-8）——只断类型。
