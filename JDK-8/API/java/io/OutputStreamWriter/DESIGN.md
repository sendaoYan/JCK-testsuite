# java.io.OutputStreamWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(OutputStream)`、`(OutputStream, String charsetName)`、`(OutputStream, Charset)`。
- 写：`write(int)`、`write(String)`、`write(String,int,int)`、`write(char[],int,int)`、`append(char)`、`append(CharSequence)`、`flush()`。
- 编码：`getEncoding()` 返回所用字符集历史名。

## 2. Java 用户如何使用
- `new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)` 以显式编码写文本；`flush()` 前字节在缓冲中。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new OutputStreamWriter(out, StandardCharsets.UTF_8)` — `jdk8u-dev/jdk/src/share/classes/java/io/OutputStreamWriter.java`（StreamEncoder 包装）。
- 官方样板 — `jdk8u-dev/jdk/test/java/io/`（编码往返用例）。

## 4. 测试用例设计 + 自动化实现（OutputStreamWriterTest.java，纯黑盒）
黑盒场景:
1. write("Aé中") + flush → 与 `String.getBytes(UTF_8)` 精确字节一致；getEncoding 非空。
2. write(char[],off,len) + append(char) → "yz!"。
3. CharsetName 构造写 "héllo 世界" → InputStreamReader(Charset) 读回一致。
4. append(char) 链式 "abc" + flush。
- 健壮性: 显式 UTF-8，确定性；内存流。
