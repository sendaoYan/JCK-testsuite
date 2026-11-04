# java.lang.Process 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「waitFor 读一下」的冒烟。重点补：白盒结构（abstract、非接口、
> 六个 abstract 方法 getInputStream/getOutputStream/getErrorStream/waitFor/exitValue/destroy、
> 三个 concrete 方法 waitFor(timeout)/destroyForcibly/isAlive）、in-memory mock 验证流读写/退出码/
> isAlive/waitFor(timeout)/destroyForcibly，以及一个真实 `sh -c echo` 进程验证 stdout/exit/exitValue。
> 断言确定，headless 安全，不 spawn 会挂起的进程。

## 1. API 提供了哪些功能

- **抽象类**：`java.lang.Process`（abstract，Object 超类；JDK-8 不实现 AutoCloseable，Java 9+ 才 implements）。
- **子进程 I/O**：`getInputStream`（读子进程 stdout）、`getOutputStream`（写子进程 stdin）、
  `getErrorStream`（读 stderr），三者 abstract。
- **终止/退出**：`waitFor()`、`exitValue()`、`destroy()`（abstract）；`waitFor(long,TimeUnit)`、
  `destroyForcibly()`（默认实现 destroy() 并返回 this）、`isAlive()`（concrete）。
- **JDK-8 无**（Java 9+）：`pid`/`toHandle`/`info`/`children`/`descendants`/`onExit`/`supportsNormalTermination`/
  `inputReader`/`outputWriter`/`waitFor(Duration)`。

## 2. Java 用户如何使用

- `ProcessBuilder.start()` 返回 `Process`；`getInputStream()` 读 stdout、`waitFor()` 等待退出码。
- `isAlive()` 判断是否仍在运行；`destroy()` 温和终止、`destroyForcibly()` 强制终止（默认委托 destroy）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Process.html`）：六个 abstract 方法、waitFor(timeout)、
  destroyForcibly/isAlive 默认实现。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Process.java`。
- **实测 JDK-8 `javap`**：确认 abstract/concrete 方法清单与 Java 9+ 差异。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/ProcessBuilder`。

## 4. 测试用例设计（映射到 ProcessTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、非接口、六个 abstract 方法、三个 concrete 方法 | testWhiteBox |
| P0 | mock：getInputStream/getErrorStream 返回字节、getOutputStream 捕获写入 | testMockStreams |
| P0 | mock：waitFor/exitValue 返回退出码、isAlive false、waitFor(timeout) true | testMockExitAndAlive |
| P0 | mock：destroyForcibly 返回 this 并触发 destroy | testDestroyForcibly |
| P0 | 真实进程：`sh -c "echo hello"` stdout/exit 0/exitValue/isAlive/destroyForcibly | testRealProcess |

## 关键规范细节（易错，已锁入断言）

- **六个 abstract 方法**：`getInputStream`/`getOutputStream`/`getErrorStream`/`waitFor`/`exitValue`/`destroy`。
- **`destroyForcibly()` 默认实现**：调用 `destroy()` 并返回自身；`isAlive()` 默认实现（基于 exitValue）concrete。
- **`waitFor(long,TimeUnit)`**：已退出进程立即返回 true。
- **JDK-8 `Process` 不 implements `AutoCloseable`**（Java 9+ 才加 `close()`）。

## 注意事项

- 真实进程只用 `sh -c "echo hello"`（立即退出，不挂起）；其余用 in-memory mock。
- headless 安全，不加 `@key`；不绑定端口、不联网、不 System.exit。
