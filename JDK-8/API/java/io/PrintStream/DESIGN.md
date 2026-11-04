# java.io.PrintStream 测试方案 (DESIGN)

## 1. API 功能
- 构造：`(OutputStream, boolean autoFlush, String encoding)`（JDK 8 无 `(..., Charset)` 与 `charset()`，均 JDK 10+）。
- 输出：`print/println`（各类型）、`printf/format`（Locale 重载）、`append`；`flush()`、`checkError()`。

## 2. Java 用户如何使用
- `System.out.printf(Locale.US, "%d%n", n)` 等格式化输出；错误静默记入 checkError 而非抛 IOException。

## 3. 开源代码实际用法调研
- 格式化实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PrintStream.java`（Formatter 委托）。

## 4. 测试用例设计 + 自动化实现（PrintStreamTest.java，纯黑盒）
黑盒场景（显式 "UTF-8" 编码 + Locale.US 保证确定性）:
1. printf 定点小数 + 返回 this；println/print/append 内容；checkError false。
2. format("%05d") → "00042"。
3. UTF-8 println 非 ASCII → 精确字节 + 平台行分隔符。
4. 空 println 仅输出行分隔符。
- 健壮性: 显式 UTF-8 + Locale.US；无反射白盒（JDK 8 无 charset() 方法）。
