# java.io.BufferedOutputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `BufferedOutputStream(OutputStream)`、`BufferedOutputStream(OutputStream,int)`
- 写: `write(int)`、`write(byte[],int,int)`
- 刷新/关闭: `flush()`、`close()`

## 2. Java 用户如何使用
- `new BufferedOutputStream(fos)` 给底层输出流加缓冲区；写完后 `flush()` 确保落盘。
- 组合链路：包装 `ByteArrayOutputStream` → 多次 write → flush → 校验缓冲被刷到目标流；close 也触发 flush。

## 3. 开源代码实际用法调研（真实片段）
- `BufferedOutputStream bos = new BufferedOutputStream(jos);` — `jdk25u-dev/test/jdk/tools/launcher/BigJar.java:136`
- `new PrintStream(new BufferedOutputStream(baos))` — `jdk25u-dev/test/jdk/tools/launcher/modules/addreads/AddReadsTestWarningError.java:116`
- `new BufferedOutputStream(new FailingCloseOutputStream())` — `jdk25u-dev/test/jdk/java/io/etc/FailingFlushAndClose.java:299`

## 4. 测试用例设计 + 自动化实现（指向 BufferedOutputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: write(int)/write(byte[],off,len) 写缓冲；flush 后目标流可见全部字节；小缓冲区容量自动冲刷；close 后 flush 语义。
- 边界: 空 write 与 close 后目标流内容正确。
- 健壮性: 固定字节输入；目标流为内存流，无全局副作用。
- 白盒 `testWhiteBox`: 继承 FilterOutputStream、write(int)/flush/close 存在。
