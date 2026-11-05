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

## 契约深化（2026-11）

### API 功能
- 命令装配：`command(String...)`/`command(List)`/`command()` 的往返、链式返回 this、返回列表的活性（live view）。
- 环境与目录：`environment()` 可变 Map 的 put/remove/get 语义与跨 builder 独立性；`directory()` 默认 null 与往返。
- 重定向族：`redirectInput/Output/Error` 的 Redirect 与 File 形态、`Redirect.from/to/appendTo` 工厂、`Redirect.Type` 枚举（PIPE/INHERIT/READ/WRITE/APPEND 共 5 值，两侧一致）、`inheritIO`、`redirectErrorStream`。
- 真子进程闭环：`java.home/bin/java -version`、样本类 Out/Cat/Env 的 stdout 重定向文件 round-trip、appendTo 计数、redirectInput 文件喂入、环境变量注入与 cwd 验证。

### 使用方式
子进程一律用 `System.getProperty("java.home")+"/bin/java"`（JDK-8 指向 `<jdk>/jre`，javac 需回退到 `<jdk>/bin/javac`），避免跨 JDK 混淆；样本源码写入 `/tmp/lcG-pb8-<nano>` 独立目录，由同一 JDK 的 javac 编译，main 的 finally 递归删除；所有子进程均 waitFor。

### 参考来源
Oracle javadoc `java.lang.ProcessBuilder`（Redirect 语义、environment 快照说明）；两侧 OpenJDK 实测（Temurin 8u372 / 27.0.1）。

### 测试用例设计 + 易错规范细节
- `command(List)`/List 构造器**按引用保存调用方列表**（`pb.command() == callerList`），varargs 形态返回可变内部列表——传 `Arrays.asList` 后 `add` 会 UOE（8 与 27 实测一致，锁定）。
- `Redirect.appendTo(f).type()` 是 `Type.APPEND`（不是 WRITE），`to != appendTo` 但各自 equals/hashCode 稳定；`PIPE/INHERIT.file()==null`、toString 分别为 "PIPE"/"INHERIT"，to/appendTo 的 toString 描述含 "write to file"/"append"。
- JDK-8 无 `Redirect.DISCARD`（JDK-9+ 才有，反射锁定 NoSuchFieldException）；`startPipeline` 缺席（既有断言）。
- 负例实测：命令不存在 → IOException `Cannot run program "名字": error=2, No such file or directory`（27 为 `Exec failed, error: 2 (...)`，前缀 `Cannot run program "<name>"` 两侧一致故锁前缀）；空命令 start → ArrayIndexOutOfBoundsException（8 消息 "[0]"，27 为 "[Index 0 out of bounds for length 0]"，只锁类型）；目录不存在 → IOException 含 `(in directory "<dir>")`；`command((String[])null)`/`command((List)null)`/`new ProcessBuilder((List)null)`/`redirectXxx(null)`/`Redirect.from/to/appendTo(null)` 均 NPE。
- `environment()` 同一 builder 返回同一 live Map（`==` 成立）；put(null,·)/put(·,null) NPE；初值 PATH 与 `System.getenv("PATH")` 相等。
- 环境闭环：父进程 put LCG_VAR → 子进程 Env 打印同值；directory() 与子进程 `new File(".").getCanonicalPath()` 精确相等。
- `ProcessBuilder.toString()` 是 Object 默认形态（`java.lang.ProcessBuilder@hex`），不含命令内容。
