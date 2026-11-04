# java.lang.ProcessBuilder 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「start true 读退出码」的冒烟。重点补：白盒结构（final、
> 两个构造器 (List)/(String...)、JDK-8 无 startPipeline）、command 三重重载 + null NPE、environment
> 活 map、directory get/set、redirectInput/Output/Error 的 PIPE/INHERIT 往返、redirectErrorStream、
> Redirect.Type/from/to/file/equals/hashCode，以及一个真实 `sh -c echo` 进程。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.ProcessBuilder`（final），两个构造器 `(List<String>)` 与 `(String...)`。
- **命令/环境/目录**：`command()`/`command(List)`/`command(String...)`、`environment()`、
  `directory()`/`directory(File)`。
- **重定向**：`redirectInput/Output/Error` 的 get/set（`Redirect` 或 `File` 参数）、`inheritIO()`、
  `redirectErrorStream()`/`redirectErrorStream(boolean)`。
- **启动**：`start()` 返回 `Process`。
- **内嵌枚举**：`ProcessBuilder.Redirect`（`PIPE`/`INHERIT`、`from(File)`/`to(File)`、`type()`/`file()`）。
- **JDK-8 无**（Java 9+）：`startPipeline`、`Redirect.DISCARD`、`Redirect.appendTo`。

## 2. Java 用户如何使用

- `new ProcessBuilder("sh", "-c", "echo hi").redirectErrorStream(true).start()` 启动子进程。
- `environment()` 增删环境变量；`directory(File)` 设工作目录；`inheritIO()` 让子进程共享标准流。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/ProcessBuilder.html`）：构造器、command、redirect、Redirect 枚举。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/ProcessBuilder.java`。
- **实测 JDK-8 `javap`**：确认 final、两个构造器、无 startPipeline、无 DISCARD/appendTo。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/ProcessBuilder`。

## 4. 测试用例设计（映射到 ProcessBuilderTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、非接口、两构造器、无 startPipeline | testWhiteBox |
| P0 | command：varargs 构造、(List)/(String...) 重载、null NPE | testCommand |
| P0 | environment：非空、含 PATH、活 map（增删可见） | testEnvironment |
| P0 | directory：默认 null、get/set 往返 | testDirectory |
| P0 | redirect：默认 PIPE、INHERIT 往返、File 重定向 type/file | testRedirects |
| P0 | redirectErrorStream：默认 false、返回 this、置 true | testRedirectErrorStream |
| P0 | Redirect 枚举：PIPE/INHERIT type、from/to 的 type/file、equals/hashCode | testRedirectTypes |
| P0 | start：`sh -c echo` 退出 0 且 stdout 捕获 | testRealStart |

## 关键规范细节（易错，已锁入断言）

- **`command(null)` 抛 `NullPointerException`**（List 版本）；`environment()` 返回可写活 map。
- **`directory()` 默认 null**（继承父进程工作目录）；`directory(File)` 往返。
- **默认重定向为 `Redirect.PIPE`**；`inheritIO()` 把三者都设为 `INHERIT`。
- **`redirectErrorStream(boolean)` 返回 this**（可链式）；默认 false。
- **`Redirect.from(File)` type=READ、`Redirect.to(File)` type=WRITE**；equals/hashCode 按 type+file。
- **JDK-8 无 `startPipeline`/`DISCARD`/`appendTo`**（Java 9+）。

## 注意事项

- 真实进程只用 `sh -c "echo hello"`（立即退出）；不重定向到磁盘文件后再依赖内容（只验证 type/file 元数据）。
- headless 安全，不加 `@key`；不绑定端口、不联网、不 System.exit。
