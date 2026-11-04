# java.util.spi.ToolProvider 测试方案

白盒结构断言在 `ToolProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public interface`，工具 SPI。
- `name()`：抽象，工具名。
- `description()`：default，返回 `Optional.empty()`。
- `run(PrintWriter, PrintWriter, String...)`：抽象，返回退出码。
- `run(PrintStream, PrintStream, String...)`：default，`requireNonNull` out/err/args 及各元素后，包 `PrintWriter` 调用抽象 run 并 flush。
- `findFirst(String)`：静态，按名字返回 `Optional<ToolProvider>`，找不到返回空 Optional。

## 2. Java 用户如何使用

- 实现 `name()` + `run(PrintWriter...)`；用 `ToolProvider.findFirst("jar")` 取工具并 `run`。
- 也可直接传 `PrintStream` 走 default 重载，自动 flush。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/jdk.jartool/share/classes/sun/tools/jar/JarToolProvider.java:33`（`name`）、`:41`（`run`）——jar 工具的 SPI 实现。
- `src/jdk.javadoc/share/classes/jdk/javadoc/internal/tool/JavadocToolProvider.java:46`（`name`）、`:58`（`run`）——javadoc 工具 SPI。
- `test/jdk/tools/jlink/JLinkToolProviderTest.java:34`：`ToolProvider.findFirst("jlink")` 取工具后运行；`test/jdk/tools/jlink/JLink100Modules.java:48`：`ToolProvider.findFirst("javac")`。

## 4. 测试用例设计 + 自动化实现

- **正常**：`name`、`description().isEmpty()`；`run(PrintWriter)` 捕获 out/err 内容与退出码（`args.length`）。
- **default run(PrintStream)**：包装 + 自动 flush，内容可见；退出码透传。
- **边界/异常**：`run(PrintStream)` 传 null err / null 参数元素 → NPE；`findFirst` 缺失名 → 空 Optional；`findFirst("javac")` 永不返回 null。
- **自动化**：`ToolProviderTest.testBlackBox()`，headless 无 `@key`。
