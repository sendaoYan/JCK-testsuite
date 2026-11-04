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

## 深化追加（契约级）

新增维度（断言 7 → 79）：与 JDK-8 侧同套（getEncoding 历史名、构造负例、精确字节编码矩阵、CountingStream 缓冲实测〔写不落底层、flush 才下沉〕、flush/close 传播计数、关闭矩阵 + getEncoding null、write 越界 + 低 16 位、append null 契约 "cABnullnuXYZYZ"、往返矩阵、单字符矩阵、大写、区域矩阵）。

JDK-27 专属：testAppendOverrides——**append(CharSequence)/append(CharSequence,int,int) 覆写**（反射确认两覆写存在）+ null 契约保持（append(null) 仍写 "null"）。

负例矩阵（实测双侧一致）：同 JDK-8 表。

版本差异：JDK-27 覆写 2 个 append 重载（JDK-8 用 Writer 默认实现，行为等价）；NPE helpful 消息按类型级断言。
