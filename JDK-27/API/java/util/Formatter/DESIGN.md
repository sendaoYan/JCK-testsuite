# java.util.Formatter 测试设计

## 1. API 功能
- 构造器：`Formatter(Appendable[, Locale])`、`Formatter(Locale)`、输出流/文件构造器。
- 格式化：`format(String, Object...)`、`format(Locale, String, Object...)`。
- 转换说明符：`%d %s %x %f %b %c %%`、宽度/精度/参数索引。
- 后端访问：`out()`、`locale()`、`toString`、`flush`、`close`、`ioException`。
- 便捷：`String.format` 委托 Formatter。

## 2. Java 用户如何使用
- 拼接字符串：`String.format(Locale.ROOT, "%05d", 42)`。
- 输出到缓冲区：`new Formatter(sb, Locale.US).format(...)`。
- 位置参数：`String.format("%2$d %1$s", ...)`。
- 关闭后语义：`f.close()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/PrintStream.java`：`printf`/`format` 委托 `Formatter`。
- JDK 源码 `java/io/PrintWriter.java`：内部 `Formatter formatter`。
- JDK 源码 `java/net/HostPortrange.java`：`new Formatter(...)` 生成异常消息。
- 广泛用法：日志/字符串模板 `String.format(...)`。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、实现 Closeable/Flushable、关键方法存在。
- 黑盒（固定格式串 + 固定参数 + Locale.US/ROOT，Appendable 后端 StringBuilder）：
  - `%d/%s/%x/%.2f` 精确输出、零填充/布尔/字符/百分号。
  - `String.format` 固定 Locale 输出与位置参数。
  - `out()`/`locale()` 返回后端与构造 Locale。
  - close 后 format 抛 FormatterClosedException。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 契约深化：转换符矩阵/精度/分组/异常族/getter | 见 Test 内「契约深化」区块 |
