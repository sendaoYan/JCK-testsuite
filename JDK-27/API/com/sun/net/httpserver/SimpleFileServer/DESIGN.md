# com.sun.net.httpserver.SimpleFileServer — 设计案

## 1. API 功能
- final 工具类（私有构造器），提供简单 HTTP 文件服务器组件（JDK 18，`jwebserver` 工具的编程接口）。
- `createFileHandler(Path rootDirectory)`：服务目录/文件的 handler，仅支持 GET/HEAD，其它方法 405，未知资源 404。
- `createFileServer(InetSocketAddress, Path, OutputLevel)`：绑定并创建 "/" 上下文的文件服务器（未 start）。
- `createOutputFilter(OutputStream, OutputLevel)`：后处理日志 filter，`OutputLevel.NONE` 抛 IAE。
- 嵌套枚举 `OutputLevel`：`NONE/INFO/VERBOSE`。
- 约束：root 必须存在、绝对、目录、可读，否则 IAE。

## 2. Java 用户如何使用
```java
var server = SimpleFileServer.createFileServer(addr, Path.of("/www"), OutputLevel.INFO);
server.start();
```
或复用 handler/filter 组装到自定义 `HttpServer`：
```java
server.createContext("/", SimpleFileServer.createFileHandler(root));
ctx.getFilters().add(SimpleFileServer.createOutputFilter(out, OutputLevel.INFO));
```

## 3. 开源代码实际用法调研
- JDK 源码：`~/git/jdk25u-dev/src/jdk.httpserver/share/classes/com/sun/net/httpserver/SimpleFileServer.java` 工厂委托给 `FileServerHandler` 与 `OutputFilter`。
- JDK 源码：`sun/net/httpserver/simpleserver/FileServerHandler.java` 用 `HttpHandlers.handleOrElse` 组合 GET/HEAD 与 405/501 fallback。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/com/sun/net/httpserver/simpleserver/` 下多份测试覆盖文件/目录/输出过滤。

## 4. 测试用例设计 + 自动化实现
- 白盒：final、私有构造器、三个工厂、OutputLevel 枚举三值（`SimpleFileServerTest.testWhiteBox`）。
- 黑盒（`SimpleFileServerTest.testBlackBox`）：
  - 临时目录写文件 → createFileHandler 真实回环：GET 文件 200+body、GET 目录 listing 含文件名、POST 405。
  - createFileHandler 相对路径/不存在路径 IAE。
  - createOutputFilter：NONE 抛 IAE、INFO 非空且 description 非空；用 latch 过滤器 + afterHandler 逆序语义确定性验证输出流记录 "GET"。
  - createFileServer 返回绑定 ephemeral 端口的未 start 服务器，finally stop(0)。
  - finally 清理临时目录。
