# java.util.prefs.InvalidPreferencesFormatException 测试方案

白盒结构断言在 `InvalidPreferencesFormatExceptionTest.testWhiteBox()`，黑盒行为断言在 `InvalidPreferencesFormatExceptionTest.testBlackBox()`。

## 1. API 功能

- 构造器：`InvalidPreferencesFormatException(Throwable cause)`、`InvalidPreferencesFormatException(String message)`、`InvalidPreferencesFormatException(String message, Throwable cause)`。
- 继承自 `java.lang.Exception`；表示 `Preferences.importPreferences(InputStream)` 解析 XML 失败时抛出的受检异常。

## 2. Java 用户如何使用

- 用户调用 `Preferences.importPreferences(inputStream)` 导入 XML 时以 `try/catch (InvalidPreferencesFormatException e)` 捕获格式错误。
- 构造时可用 `(String, Throwable)` 同时携带自描述消息与底层解析异常根因。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `src/java.prefs/share/classes/java/util/prefs/Preferences.java:1224-1227`：`public static void importPreferences(InputStream is) throws IOException, InvalidPreferencesFormatException` 内部委托 `XmlSupport.importPreferences(is)`。
- `src/java.prefs/unix/classes/java/util/prefs/FileSystemPreferences.java:578`：`if (e instanceof InvalidPreferencesFormatException)` 识别首选项文件 XML 格式损坏并告警。
- `test/jdk/java/util/prefs/CommentsInXml.java:58`：`ur.importPreferences(new ByteArrayInputStream(bos.toByteArray()))` 导入触发该异常路径的官方测试。

## 4. 测试用例设计 + 自动化实现

- **白盒**：public 具体类、`extends Exception`、非 `Externalizable`、恰好三个 public 构造器 `(Throwable)/(String)/(String,Throwable)`。
- **黑盒**：`(String)` 设消息无 cause；`(Throwable)` 设 cause 且消息等于 `cause.toString()`；`(String,Throwable)` 同时设消息与 cause；继承的 `initCause` 可后补 cause 并返回 this；throw/catch 往返并解包根因。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 16 断言。
