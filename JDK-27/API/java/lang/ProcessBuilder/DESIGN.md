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
