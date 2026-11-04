# javax.imageio.stream.FileImageOutputStream — DESIGN

## 1. API 功能
`FileImageOutputStream` 是 `ImageOutputStreamImpl` 的具体子类，把 `File`（内部转为 `RandomAccessFile "rw"`）或现成的 `RandomAccessFile` 包装成可随机读写的 `ImageOutputStream`。方法分组：
- 构造：`FileImageOutputStream(File f)`、`FileImageOutputStream(RandomAccessFile raf)`。
- 写：`write(int)`、`write(byte[],int,int)`；继承的 `writeByte/writeInt/writeUTF/...`。
- 读/位置/长度：`read()`、`read(byte[],int,int)`、`seek(long)`、`length()`（真实文件长度）。
- 关闭：`close()` 关闭底层 `RandomAccessFile`。

## 2. Java 用户如何使用
典型链：`new FileImageOutputStream(file)` → `writeByte/writeInt/writeBoolean/writeUTF/writeShort` 写 → `length()`/`getStreamPosition()` → `seek(0)` + `readByte/readInt/...` 读回 → `close()`；再用 `FileImageInputStream` 打开同一文件校验落盘字节。也可用 `RandomAccessFile` 构造。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/FileImageOutputStream.java`：`write(int)` 先 `flushBits()` 再 `raf.write(b)` 并 `++streamPos`；`seek()` 用 `raf.seek`。
- `src/java.desktop/share/classes/com/sun/imageio/spi/FileImageOutputStreamSpi.java`：`ImageIO.createImageOutputStream(File)` 走此实现。
- `test/jdk/javax/imageio/stream/StreamFlush.java`：覆盖 File 输出流关闭/落盘行为。

## 4. 测试用例设计 + 自动化实现（`FileImageOutputStreamTest.testBlackBox`）
- `Files.createTempFile` 构造，断言初始 `length()==0`；写 `writeByte/writeInt/writeBoolean/writeUTF("hi")/writeShort` 后断言 `length()==12`。
- `seek(0)` 逐个读回 `readByte/readInt/readBoolean/readUTF/readShort`。
- `close()` 后用 `FileImageInputStream` 校验文件长度与 `readByte/readInt`。
- 用 `RandomAccessFile("rw")` 构造第二个实例写入单字节并 `Files.readAllBytes` 校验。
- finally 删除临时文件。
