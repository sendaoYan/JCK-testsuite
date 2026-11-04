# java.util.logging.SocketHandler 测试方案

白盒结构断言在 `SocketHandlerTest.testWhiteBox()`，构造器签名反射断言在 `SocketHandlerTest.testBlackBox()`。

## 1. API 功能

- **继承**：`extends StreamHandler`。
- **构造器**：`SocketHandler()` 与 `SocketHandler(String host, int port)`（均声明 `IOException`）。
- **行为**：把格式化后的日志通过 TCP 发往指定 host:port；构造即联网，`publish` 发送记录，`close` 关闭 socket。

## 2. Java 用户如何使用

- `new SocketHandler("loghost.example.com", 5000)` 将日志远程投递。
- 本测试环境 headless 且禁止真实网络，故只做结构与构造器签名反射，不实例化。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/SocketHandler.java:59-68`：`SocketHandler()` / `SocketHandler(String,int)` 构造器与 `connect()`。
- `src/java.logging/share/classes/java/util/logging/SocketHandler.java:100-115`：`publish` 与 `close` 覆写。

## 4. 测试用例设计 + 自动化实现

- **正常（白盒）**：类继承链 StreamHandler/Handler；publish/close 方法存在。
- **组合（反射）**：无参构造器存在且声明 `IOException`；`(String,int)` 构造器参数类型与 `IOException` 声明。
- **健壮性**：不实例化、不联网、不占端口，纯 headless。
