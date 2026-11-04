# java.net.spi.URLStreamHandlerProvider 设计文档

## 1. API 功能

为自定义 URL 协议提供 `URLStreamHandler` 的 SPI 抽象类，实现 `URLStreamHandlerFactory`。

- 抽象方法：`URLStreamHandler createURLStreamHandler(String protocol)`（按协议返回 handler，不支持返回 null）。
- 构造器：protected 无参构造器。

## 2. Java 用户如何使用

- 实现类：继承并实现 `createURLStreamHandler`，经 `META-INF/services` 注册，供 `URL` 解析自定义协议时调用。
- 组合链路：`provider.createURLStreamHandler("myproto")` → 返回对应 handler 或 null。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/net/spi/URLStreamHandlerProvider.java`）：
  该 SPI 桥接 `URLStreamHandlerFactory`，为 `URL` 提供协议处理扩展点。

## 4. 测试用例设计 + 自动化实现

文件：`URLStreamHandlerProviderTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / abstract / 实现 `URLStreamHandlerFactory` / protected 无参构造器 / `createURLStreamHandler` 方法面。
- 黑盒：匿名实现 → `instanceof URLStreamHandlerFactory` → 已知协议非 null、未知协议 null。

## 5. 深化缺口分析(批次 J,2026-11-05)

原 9 断言。深化到 68 断言:

- **形状精确化**(probed):本类**零声明方法**——契约完全来自
  `URLStreamHandlerFactory` 接口(单一继承接口断言);唯一 protected 无参构造器;
  反射双门:newInstance → IllegalAccessException、setAccessible →
  InaccessibleObjectException。
- **工厂对象行为**(无全局注册):记录 protocol 参数序列(dummy/http/https/""/null
  全部原样记录);每次调用返回新 handler(无 provider 级缓存);**5 参 URL 构造器
  + handler**:`new URL("dummy","h2.example",81,"/f2",h)` 的 protocol/host/port/
  file/authority/toExternalForm 全断言(URLStreamHandler 基类 parseURL 纯本地);
  port -1 保持、外部形式省略端口;getDefaultPort()==-1。
- **全局 factory 集成**(文件内最后执行,槽位 JVM 全局一次性):
  `URL.setURLStreamHandlerFactory(provider)` 一次成功;`new URL("dummy://...")`
  全字段(host/port -1/path/query/file/authority/外部形式 round-trip)由自定义
  handler 解析;provider 观察到 "dummy";**handler 缓存**(probed):第二次
  new URL 同协议不再询问 factory;provider 对 http 返回 null → 内置 handler 接管
  (不联网);"DUMMY" scheme 被小写化后命中缓存(probed);未知协议
  `nosuchproto` → MalformedURLException("unknown protocol ...");
  **二次 set → Error("factory already defined")**、set(null) 同样 Error。
- jtreg 每文件独立 JVM,factory 全局态不外泄。

## 6. 版本差异

JDK 9+ 存在。JDK-8 反证(temurin jdk8u372-b07):

```
$ javap -public java.net.spi.URLStreamHandlerProvider
Error: class not found: java.net.spi.URLStreamHandlerProvider
```

JDK-8 侧等价扩展点为 `java.net.URLStreamHandlerFactory`(已在其它批次覆盖)。
不做 JDK-8 镜像。
