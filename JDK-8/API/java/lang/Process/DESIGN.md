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

## 契约深化（2026-11）

### API 功能
- 生命周期：`isAlive`/`exitValue`/`waitFor()`/`waitFor(long,TimeUnit)`/`destroy`/`destroyForcibly` 在真子进程上的完整语义（运行中 ITSE、销毁后非零退出、退出码稳定性）。
- 流契约：四个流非 null；stdin 写入回显（Cat）；`redirectErrorStream(true)` 后 `getErrorStream()` 立即 EOF。
- 退出码精度：`System.exit(N)` 逐码表（0/3/7/200/255）。
- 白盒：JDK-8 的 API 面锁定（waitFor(long,TimeUnit)/isAlive/destroyForcibly 存在；pid/toHandle/info/onExit/children/descendants/supportsNormalTermination/close/inputReader/waitFor(Duration) 均不存在）。

### 使用方式
子进程一律用 `System.getProperty("java.home")+"/bin/java"`（JDK-8 的 java.home 是 `<jdk>/jre`，javac 在 `<jdk>/bin`）；样本类 Sleeper（sleep 10000）/ExitN/Cat 写入 `/tmp/lcG-p8-<nano>` 由同一 JDK javac 编译，finally 递归删除；每个子进程必 waitFor（Sleeper 先 destroy 再 waitFor），不留悬挂。

### 参考来源
Oracle javadoc `java.lang.Process`；两侧 OpenJDK 实测（Temurin 8u372 / 27.0.1）。

### 测试用例设计 + 易错规范细节
- 运行中 `exitValue()` → `IllegalThreadStateException`，**消息两侧一致为 "process hasn't exited"**，逐字锁定。
- `destroy()`（SIGTERM）退出码 143、`destroyForcibly()`（SIGKILL）为 137——Linux 实测值，断言只锁 `!= 0`（信号映射属实现细节）。
- `waitFor(200ms)/waitFor(0)/waitFor(-5)` 运行中均 false；`waitFor(1, null unit)` → NPE（8 消息 "null"，27 为 helpful 消息，只锁类型）；退出后 `waitFor(timeout)` true、`waitFor()` 稳定返回同码。
- `redirectErrorStream(true)` 时 `getErrorStream()` 读到 EOF（长度 0）——流互斥核心契约。
- Cat 闭环：写 "lcgCat42\n" + flush + close → 读回精确同串 + 平台换行结尾，exit 0。
- GateProcess（自造 mock，exitValue 未完成前抛 ITSE）证明具体 `isAlive()`/`waitFor(timeout)` 由 `exitValue()` 派生。
- 白盒锁定 JDK-8 缺席的 9+/17+/19+ 成员（NoSuchMethodException 负例）。
