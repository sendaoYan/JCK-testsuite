# java.io.BufferedInputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `BufferedInputStream(InputStream)`、`BufferedInputStream(InputStream,int)`
- 读: `read()`、`read(byte[],int,int)`、`readAllBytes()`、`readNBytes(byte[],int,int)`
- 定位: `skip(long)`、`available()`、`markSupported()`、`mark(int)`、`reset()`
- 关闭: `close()`

## 2. Java 用户如何使用
- `new BufferedInputStream(new FileInputStream(f))` 给底层流加缓冲区；`mark/read/reset` 支持回看（mark limit 内）。
- 组合链路：包装 `ByteArrayInputStream` → `read`/`skip`/`mark`/`reset` 顺序验证回看语义 → EOF=-1。

## 3. 开源代码实际用法调研（真实片段）
- `BufferedInputStream bis = new BufferedInputStream(fis)) {` — `jdk25u-dev/test/jdk/tools/launcher/BigJar.java:92`
- `new BufferedInputStream(new FileInputStream(srcZip)))) {` — `jdk25u-dev/test/jdk/build/translations/VerifyTranslations.java:92`
- `new BufferedInputStream(new ByteArrayInputStream(...))` — `jdk25u-dev/test/jdk/tools/jpackage/helpers/jdk/jpackage/test/MacSign.java:400`

## 4. 测试用例设计 + 自动化实现（指向 BufferedInputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: markSupported==true；从新鲜流 skip(2)==2 后 read==3；mark→read→reset→重放同值→read 下一个→EOF=-1（避免“buffered skip 只跳缓冲区内字节”的陷阱，见文件内 skip 修复）。
- 边界: read 到 EOF；reset 重放字节一致。
- 健壮性: 固定字节输入；无墙钟/随机/全局状态。
- 白盒 `testWhiteBox`: 继承 FilterInputStream、readAllBytes/readNBytes 存在。
