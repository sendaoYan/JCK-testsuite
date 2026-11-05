# ProcessBuilder — 设计方案

## 1. API 功能
`java.lang.ProcessBuilder`（final）构造并启动本地进程：
- 构造：`ProcessBuilder(List<String>)`、`ProcessBuilder(String...)`
- 命令：`command()/command(List)/command(String...)`
- 环境：`environment()` 返回可变环境 Map
- 工作目录：`directory()/directory(File)`
- 重定向：`redirectInput/Output/Error` + `inheritIO` + `redirectErrorStream`
- 启动：`start()`、静态 `startPipeline`

## 2. Java 用户如何使用
- 用命令 + 参数构造，配置目录/环境后 `start()`，返回 `Process`。
```java
ProcessBuilder pb = new ProcessBuilder("git", "status");
pb.directory(new File("/path/repo"));
pb.redirectErrorStream(true);
Process p = pb.start();
```

## 3. 开源代码实际用法调研
- 现代 JDK 推荐用 `ProcessBuilder` 替代 `Runtime.exec`（可读性/参数列表无 shell 转义）。
- JDK `test/jdk/java/lang/ProcessBuilder` 系列验证命令回读、环境、重定向、错误处理。
- 本测试不执行真实外部命令（跨平台健壮）：仅构造 + 回读 + 缺失命令 `start()` 抛 `IOException`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证 final、两个公开构造器存在。
黑盒：`command()` 三态回读；`environment()` 可读可写（唯一键）；`directory` 临时目录往返；重定向默认 `PIPE`、`redirectErrorStream` 开关、`inheritIO` 不抛；缺失命令 `start()` 抛 `IOException`。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### API 功能
- 命令装配：`command(String...)`/`command(List)`/`command()` 的往返、链式返回 this、返回列表的活性（live view）。
- 环境与目录：`environment()` 可变 Map 的 put/remove/get 语义与跨 builder 独立性；`directory()` 默认 null 与往返。
- 重定向族：`redirectInput/Output/Error` 的 Redirect 与 File 形态、`Redirect.from/to/appendTo` 工厂、`Redirect.Type` 枚举（PIPE/INHERIT/READ/WRITE/APPEND 共 5 值）、`Redirect.DISCARD`（JDK-9+）、`redirectErrorStream`。
- 真子进程闭环：`java.home/bin/java -version`、样本类 Out/Cat/Env 的 stdout 重定向文件 round-trip、appendTo 计数、redirectInput 文件喂入、环境变量注入与 cwd 验证、`startPipeline` 两级真管道（Out | Cat）。

### 使用方式
子进程一律用 `System.getProperty("java.home")+"/bin/java"`（27 直接在 `<jdk>/bin` 下），避免跨 JDK 混淆；样本源码写入 `/tmp/lcG-pb27-<nano>` 独立目录，由同一 JDK 的 javac 编译，main 的 finally 递归删除；所有子进程均 waitFor。

### 参考来源
Oracle javadoc `java.lang.ProcessBuilder`（Redirect/DISCARD 语义、environment 快照说明）；OpenJDK 27 实测（Temurin 27.0.1）。

### 测试用例设计 + 易错规范细节
- `command(List)`/List 构造器**按引用保存调用方列表**（`pb.command() == callerList`），varargs 形态返回可变内部列表（与 JDK-8 一致，实测锁定）。
- `Redirect.appendTo(f).type()` 是 `Type.APPEND`（不是 WRITE）；`Redirect.DISCARD`：type WRITE、Linux 上 `file()` 为 `/dev/null`、toString "WRITE"、redirectXxx(DISCARD) 保持同一常量身份；`java -version` 双 DISCARD 重定向 exit 0。
- `startPipeline` 真管道正例：Out|Cat 末级 stdout 精确等于首级标记 `LCG_MARKER_7`，两级 exitValue 均 0（此前只测了空表/IOException/IAE 负例）。
- 负例实测：命令不存在 → IOException 消息 `Cannot run program "名字": Exec failed, error: 2 (No such file or directory)`（8 为 `error=2, No such file or directory`，锁共同前缀 `Cannot run program "<name>"`）；空命令 start → ArrayIndexOutOfBoundsException（消息含 "Index 0 out of bounds for length 0"，8 为 "[0]"，只锁类型）；目录不存在 → IOException 含 `(in directory "<dir>")`；各类 null 参数均 NPE。
- `environment()` 同一 builder 返回同一 live Map；put(null,·)/put(·,null) NPE（27 消息为 helpful 形式 `Cannot invoke "String.indexOf(int)"...`，与 8 的 "null" 不同，只锁类型）。
- 环境闭环：父进程 put LCG_VAR → 子进程 Env 打印同值；directory() 与子进程 canonical cwd 精确相等。
- `ProcessBuilder.toString()` 为 Object 默认形态，不含命令内容；`startPipeline` 反射存在性锁定。
