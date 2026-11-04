# java.util.prefs.BackingStoreException 测试方案

白盒结构断言在 `BackingStoreExceptionTest.testWhiteBox()`，黑盒行为断言在 `BackingStoreExceptionTest.testBlackBox()`。

## 1. API 功能

- 构造器：`BackingStoreException(String message)`、`BackingStoreException(Throwable cause)`。
- 继承自 `java.lang.Exception`；表示首选项后备存储读写失败时抛出的受检异常。

## 2. Java 用户如何使用

- 实现方在 `flushSpi`/`syncSpi`/`removeNodeSpi` 等后备存储操作失败时抛出：`throw new BackingStoreException(cause)`。
- 用户调用 `flush()`/`sync()`/`nodeExists()` 等声明 `throws BackingStoreException` 的方法时以 `try/catch` 捕获并 `getCause()` 解包根因。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `src/java.prefs/unix/classes/java/util/prefs/FileSystemPreferences.java`：`flushSpi`/`syncSpi` 在后备文件失败时抛出 `BackingStoreException`（该类是平台默认实现的 SPI 填充者）。
- `test/jdk/java/util/prefs/ConflictInFlush.java:45`：`catch (BackingStoreException bse)` 捕获刷盘冲突。
- `test/jdk/java/util/prefs/ExportNode.java:44`：方法签名 `throws BackingStoreException, IOException`，`exportNode` 传播该异常。
- `src/jdk.jshell/share/classes/jdk/internal/jshell/tool/JShellToolBuilder.java:34`：`import java.util.prefs.BackingStoreException`。

## 4. 测试用例设计 + 自动化实现

- **白盒**：public 具体类、`extends Exception`、恰好两个 public 构造器 `(String)` 与 `(Throwable)`。
- **黑盒**：`(String)` 设 `getMessage` 且 `getCause==null`；`(Throwable)` 设 `getCause` 且 `getMessage==cause.toString()`；throw/catch 往返；解包嵌套根因类型。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 13 断言。
