# java.io.OutputStreamWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造: `OutputStreamWriter(OutputStream)`、`OutputStreamWriter(OutputStream,String)`、`OutputStreamWriter(OutputStream,Charset)`、`OutputStreamWriter(OutputStream,CharsetEncoder)`
- 写: `write(int)`、`write(char[],int,int)`、`write(String,int,int)`
- 编码/控制: `getEncoding()`、`flush()`、`close()`

## 2. Java 用户如何使用
- `new OutputStreamWriter(out, StandardCharsets.UTF_8)` 把字符按字符集编码为字节；`flush` 确保编码器剩余字节刷出。
- 组合链路：写字符串/字符数组 → flush → 底层流 `toByteArray` 得到 UTF-8 字节。

## 3. 开源代码实际用法调研（真实片段）
- `new OutputStreamWriter(new FileOutputStream(file), "UTF-8")` — `jdk25u-dev/test/jdk/tools/launcher/UnicodeTest.java:246`
- `new OutputStreamWriter(new FileOutputStream(file), charset)` — `jdk25u-dev/test/jdk/java/io/FileWriter/ConstructorTest.java:165`
- `new OutputStreamWriter(outputStream)` — `jdk25u-dev/test/jdk/java/io/Writer/Bug6856817.java:113`

## 4. 测试用例设计 + 自动化实现（指向 OutputStreamWriterTest.java 黑盒 `testBlackBox`）
- 正常/组合: write(String)/write(char[],off,len)/write(int) → flush → 目标流字节 == 预期 UTF-8 编码；getEncoding 规范名（实测 "UTF8"）。
- 边界: 空字符串写 0 字节；非 ASCII 字符多字节正确。
- 健壮性: 显式 UTF-8 Charset；内存流目标；无默认编码依赖。
- 白盒 `testWhiteBox`: 继承 Writer、getEncoding/flush/close 存在。
