# java.io.PrintWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造：`(Writer)`、`(OutputStream)`、`(OutputStream, boolean)` 等（JDK 8 无 `(OutputStream, boolean, Charset)`，JDK 10+）。
- 输出：`print/println`（各类型）、`printf/format`（Locale 重载）、`append`；`flush()`、`checkError()`。

## 2. Java 用户如何使用
- 字符侧格式化输出（日志/文本），从不抛 IOException（错误记入 checkError）；需显式编码时用 OutputStreamWriter 包装。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PrintWriter.java`。

## 4. 测试用例设计 + 自动化实现（PrintWriterTest.java，纯黑盒）
黑盒场景:
1. StringWriter + printf 定点小数 + 返回 this；print(boolean/long) 内容；checkError false。
2. format("%05d") → "00042"。
3. 显式 UTF-8（OutputStreamWriter 包装）println 非 ASCII → InputStreamReader 读回精确 + 平台行分隔符。
- 健壮性: Locale.US + 显式 UTF-8；无反射白盒。
