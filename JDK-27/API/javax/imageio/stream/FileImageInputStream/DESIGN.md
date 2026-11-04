# javax.imageio.stream.FileImageInputStream — DESIGN

## 1. API 功能
`FileImageInputStream` 是 `ImageInputStreamImpl` 的具体子类，把 `File`（内部转为 `RandomAccessFile "r"`）或现成的 `RandomAccessFile` 包装成可随机读的 `ImageInputStream`。方法分组：
- 构造：`FileImageInputStream(File f)`、`FileImageInputStream(RandomAccessFile raf)`。
- 读：`read()`、`read(byte[],int,int)`；继承的 `readByte/readInt/readFully/readUnsignedByte/...`。
- 位置/长度：`seek(long)`、`length()`（真实文件长度）、`getStreamPosition()`。
- 字节序：继承的 `setByteOrder/getByteOrder`（影响 `readShort/readInt/readLong/...`）。
- 关闭：`close()` 关闭底层 `RandomAccessFile`。

## 2. Java 用户如何使用
典型链：`new FileImageInputStream(new File(path))` → `length()` 取长度 → `read()`/`readByte()` 顺序读 → `seek(0)` 回退 → `setByteOrder(LITTLE_ENDIAN)` 后 `readInt()` 按小端解释 → `mark()/readFully()/reset()` → `close()`。或用 `RandomAccessFile` 构造以复用已打开的随机访问句柄。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/FileImageInputStream.java`：`read()` 直接 `raf.read()` 并 `++streamPos`；`seek()` 调用 `raf.seek(pos)` 后 `streamPos = raf.getFilePointer()`。
- `src/java.desktop/share/classes/com/sun/imageio/spi/FileImageInputStreamSpi.java`：`ImageIO.createImageInputStream(File)` 走此实现。
- `test/jdk/javax/imageio/stream/FileImageStreamPos.java`：验证 `File`/`RandomAccessFile` 两种构造下的位置跟踪。

## 4. 测试用例设计 + 自动化实现（`FileImageInputStreamTest.testBlackBox`）
- `Files.createTempFile` 写 16 字节（1..16），断言 `length()==16`、`read/readByte/readUnsignedByte`。
- `readInt` 大端 `0x01020304`；`setByteOrder(LITTLE_ENDIAN)` 后 `readInt` 为 `0x04030201`。
- `mark()` + `readFully` + `reset()` 回退；`skipBytes(10)`；`flushBefore(4)` + `getFlushedPosition()`。
- 用 `RandomAccessFile("r")` 构造第二个实例验证长度与读取。
- finally 删除临时文件。
