# java.rmi.server.LogStream 行为测试设计

## 1. API 功能
- 工厂：`static LogStream log(String name)`（按名字缓存）；`static PrintStream getDefaultStream()`/`setDefaultStream`。
- 查询/转换：`getOutputStream()`/`setOutputStream(OutputStream)`、`toString()`（返回名字）。
- 等级：`SILENT=0`、`BRIEF=10`、`VERBOSE=20`；`static int parseLevel(String)`。
- 继承：`extends PrintStream`；整类 `@Deprecated`。

## 2. Java 用户如何使用
- `LogStream.log("myapp")` 取/建命名日志流；`LogStream.parseLevel("BRIEF")` 解析等级。
- `setOutputStream` 重定向日志输出；`getOutputStream` 取当前输出流。
- 等级解析：数字字符串按整数解析，`SILENT/BRIEF/VERBOSE` 前缀匹配。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/LogStream.java`：`defaultStream = System.err`；`parseLevel` 用 `startsWith` 前缀匹配。
- JDK 测试 `test/jdk/java/rmi/testlibrary/TestSocketFactory.java`：`LogStream` 与 RMI 测试日志库配合。
- RMI 运行时 `sun.rmi.server.UnicastServerRef` 用 `LogStream.log("sun.rmi.transport.tcp")` 记录调用日志。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、父类 PrintStream、`@Deprecated`。
- 黑盒：等级常量；`parseLevel`（全名/数字/null/空串/未知）；`log` 命名与缓存同一性；`getDefaultStream` 非 null；`set/getOutputStream` 往返；`write`/`flush` 可调用（无换行不产生外部输出）。
- 实现见 `LogStreamTest.java`。
