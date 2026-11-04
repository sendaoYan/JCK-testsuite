# java.net.http.HttpClient.Builder 设计文档

## 1. API 功能

`HttpClient` 的配置构造器（`HttpClient.newBuilder()` 返回）。

- 设置：`cookieHandler`、`connectTimeout(Duration)`、`sslContext`、`sslParameters`（防御性拷贝）、
  `executor`、`followRedirects(Redirect)`、`version(Version)`、`priority(int)`、`proxy(ProxySelector)`、
  `authenticator(Authenticator)`、`localAddress(InetAddress)`（JDK-27 新增，仅 builder 侧）。
- `build()` 产出 `HttpClient`；同一 builder 可多次 build，互不影响且仍可继续修改。

## 2. 使用方式

`HttpClient.newBuilder().version(Version.HTTP_3).connectTimeout(Duration.ofSeconds(2)).build()`。

## 3. 参考来源

- JDK-27 `javap -public java.net.http.HttpClient$Builder`；Temurin jdk-27.0.1.0.2+1 源码
  （`jdk/internal/net/http/HttpClientBuilderImpl.java`）。
- 规范：java.net.http 包文档（JDK 11+ API，JDK-27 扩展 HTTP_3/localAddress）。

## 4. 测试用例设计 + 易错规范细节

文件：`BuilderTest.java`。

- 返回 this：全部 setter 链式返回自身。
- 负例矩阵（精确异常）：9 个 setter 的 null → NPE；`connectTimeout(ZERO/-1ms)` → IAE
  "Invalid duration"；`priority(0/257/-1/MIN/MAX)` → IAE，1/256 合法；1ns 超时合法。
- 默认值：fresh builder `build()` 后 version=HTTP_2、redirect=NEVER、Optional 字段全空。
- 拷贝语义：`sslParameters(...)` 读回内容相等但非同一实例（防御性拷贝，probed）；
  其余引用类型读回为同一实例（identity）。
- `proxy(ProxySelector)` 接受 `NO_PROXY`（null 时 HttpClient 内部处理直连）。
- builder 复用：两次 build 得到不同实例，先前者不受后配置影响，builder 仍可变。
- `localAddress(null)` 容忍（清除），`localAddress(loopback)` 可 build（probed）；
  `version(HTTP_3)` 可 build。

## 5. 版本差异

- JDK-27：新增 `localAddress(InetAddress)`；`priority` 上限 256（0/257 均拒绝）；
  `HTTP_3` 枚举可配置。JDK-8 无此包（java.net.http 自 JDK 11 引入）。
