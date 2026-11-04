# java.util.logging.FileHandler 测试方案

白盒结构断言在 `FileHandlerTest.testWhiteBox()`，黑盒行为断言在 `FileHandlerTest.testBlackBox()`。

## 1. API 功能

- **继承**：`extends StreamHandler`；5 个 public 构造器（无参 / `(String)` / `(String,boolean)` / `(String,int,int)` / `(String,long,int,boolean)`）。
- **行为**：按 pattern 写滚动日志文件（`%g` 代、`%u` 唯一、`%h` 家目录），支持 limit/count/append；默认等级 ALL、默认格式器 XMLFormatter。

## 2. Java 用户如何使用

- `new FileHandler("/var/log/app%g.log", 100_000, 5, true)` 滚动写日志。
- 参数校验：pattern 空 / limit<0 / count<1 抛 IllegalArgumentException。
- `fh.publish(record); fh.close();` 后检查生成文件。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/IsLoggableHandlerTest.java:54`：`new FileHandler(logfile.getAbsolutePath())`。
- `test/jdk/java/util/logging/CheckZombieLockTest.java:137-138`：`new FileHandler("%t/.../log.log")` + `publish(new LogRecord(...))`。
- `test/jdk/java/util/logging/FileHandlerPatternExceptions.java:151-153`：`new FileHandler()` 抛 RuntimeException、`new FileHandler("")` 抛 IAE、`new FileHandler(null)` 抛 NPE。

## 4. 测试用例设计 + 自动化实现

- **异常**：空 pattern / count<1 / limit<0 抛 IllegalArgumentException（不碰真实文件）。
- **正常**：临时目录 + 唯一 pattern 构造，默认等级 ALL、格式器 XMLFormatter；publish→flush→close 后断言目录中产生非空文件。
- **健壮性**：`Files.createTempDirectory("jck")` 隔离，finally 递归清理（含 .lck）。
