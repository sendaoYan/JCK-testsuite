# Process — 设计方案

## 1. API 功能
`java.lang.Process`（abstract）表示一个本地进程，实现 `Closeable`：
- 抽象：`getOutputStream/getInputStream/getErrorStream/waitFor/exitValue/destroy`
- 便捷流：`inputReader/errorReader/outputWriter`（final）
- 状态：`isAlive/pid/supportsNormalTermination/waitFor(long,TimeUnit)/waitFor(Duration)`
- 句柄：`toHandle/info/children/descendants/onExit`

## 2. Java 用户如何使用
- 通常由 `ProcessBuilder.start()` 或 `Runtime.exec()` 返回，读输出、等待退出。
```java
Process p = new ProcessBuilder("java", "-version").start();
try (BufferedReader r = p.inputReader()) { String line; while ((line = r.readLine()) != null) { ... } }
int code = p.waitFor();
```

## 3. 开源代码实际用法调研
- `ProcessBuilder` 是推荐方式（`Runtime.exec` 已不推荐）；JDK `test/jdk/java/lang/ProcessBuilder` 大量验证输入/输出/退出码。
- 本测试用内存 `MockProcess` 子类覆盖抽象方法，避免执行真实外部命令（跨平台健壮）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Process")` 验证 abstract、实现 `Closeable`、五个抽象方法。
黑盒：内存子类验证 `inputReader/errorReader` 读流、`outputWriter` 写流、`waitFor/exitValue/isAlive` 退出状态、`waitFor(Duration)`。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### API 功能
- 生命周期：`isAlive`/`exitValue`/`waitFor()`/`waitFor(long,TimeUnit)`/`waitFor(Duration)`/`destroy`/`destroyForcibly`/`close` 在真子进程上的完整语义。
- 9+ 桥接 API：`pid()`、`toHandle()`（同实例、pid 一致）、`supportsNormalTermination()`、`info()`（活跃子进程 command 精确等于所启 java 路径、commandLine 含类名、user 等于 user.name）。
- 流契约：四个流非 null；stdin 写入回显（Cat）；`redirectErrorStream(true)` 后 `getErrorStream()` 立即 EOF。
- 退出码精度：`System.exit(N)` 逐码表（0/3/7/200/255）。

### 使用方式
子进程一律用 `System.getProperty("java.home")+"/bin/java"`；样本类 Sleeper/ExitN/Cat 写入 `/tmp/lcG-p27-<nano>` 由同一 JDK javac 编译，finally 递归删除；每个子进程必 waitFor（Sleeper 先 destroy 再 waitFor），不留悬挂。

### 参考来源
Oracle javadoc `java.lang.Process`（pid/toHandle/info/onExit 桥接语义）；OpenJDK 27 实测（Temurin 27.0.1）。

### 测试用例设计 + 易错规范细节
- 运行中 `exitValue()` → `IllegalThreadStateException`，**消息两侧一致为 "process hasn't exited"**，逐字锁定。
- `destroy()` 退出码 143、`destroyForcibly()` 137（Linux 实测），断言只锁 `!= 0`。
- `waitFor(Duration)` 两种状态：运行中 false（200ms）、退出后 true；`waitFor(1, null)` NPE 只锁类型（27 消息为 `Cannot invoke "...TimeUnit.toNanos(long)"...`）。
- 活跃子进程 `info().command()` 精确等于 `java.home/bin/java`（argv[0] 原样）、`commandLine()` 含 "Sleeper"、`user()` 等于 `System.getProperty("user.name")`；已退出进程 info 的 Optional 全空。
- `toHandle()` 每次返回同一实例、`toHandle().pid()==pid()`；`supportsNormalTermination()` 真子进程为 true（Linux）。
- 并发双子进程：pid 互异、destroy/destroyForcibly 各自非零退出。
- 白盒锁定 27 面板：pid/onExit/toHandle/info/children/descendants/waitFor(Duration)/close/inputReader/errorReader/outputWriter 均存在。
- GateProcess 证明具体 `isAlive()`/`waitFor(timeout)` 由 `exitValue()` 派生（mock 无真进程）。
