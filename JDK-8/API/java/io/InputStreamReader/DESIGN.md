# java.io.InputStreamReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(InputStream)`、`(InputStream, String charsetName)`、`(InputStream, Charset)`。
- 读：`read()`、`read(char[])`、`read(char[],int,int)`（继承 Reader）。
- 编码：`getEncoding()` 返回所用字符集历史名。

## 2. Java 用户如何使用
- `new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)` 以显式编码读文本（多字节/增补字符安全）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new InputStreamReader(in, StandardCharsets.UTF_8)` — `jdk8u-dev/jdk/src/share/classes/java/io/InputStreamReader.java`（StreamDecoder 包装）。
- 官方样板 — `jdk8u-dev/jdk/test/java/io/`（编码往返用例）。

## 4. 测试用例设计 + 自动化实现（InputStreamReaderTest.java，纯黑盒）
黑盒场景:
1. OutputStreamWriter(UTF-8) 写 "héllo 世界 😀" → 字节 → InputStreamReader(UTF-8) read(char[],off,len) 分批读回全文一致；EOF。
2. CharsetName 构造单字符读 "abc"。
3. Charset 构造 read(char[]) 逐块读 "xy" + EOF。
- 健壮性: 显式 UTF-8 编码，确定性；内存流。
