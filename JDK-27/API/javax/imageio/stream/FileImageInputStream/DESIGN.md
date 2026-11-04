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

## 5. 深化补充（testDeep* 契约覆盖，含 JDK-27 特有断言）
追加 12 个 testDeep 方法（~140 条断言）：构造矩阵（(File)null/(RandomAccessFile)null IAE、不存在文件与目录 → 精确 `FileNotFoundException`）；length 语义（文件真实长、空文件 0、close 后 -1 不抛）；三 isCached 全 false；BE/LE 双字节序全基元 + 数组 round-trip；EOF 精度（剩 1 字节 readShort / 剩 2 字节 readInt / 剩 3 字节 readLong → 精确 EOFException）；边界负例精确类；嵌套 mark/reset（入 flushed 区 → 精确 `javax.imageio.IIOException`）；skipBytes 语义；位操作（setBitOffset -1/8 IAE、seek 复位 bitOffset）；close 关闭底层 RAF（RAF 再读 IOException 实测断言）。**版本差异（JDK-27 特有）**：RandomAccessFile 构造器采纳 RAF 指针 —— `raf.seek(4)` 后 `getStreamPosition()==4`（JDK-8 实测为 0，bug 5043343 于 JDK-27 修复，上游回归测试 test/jdk/javax/imageio/stream/FileImageStreamPos.java）；两侧各自精确断言。javap 双侧公开 API 相同。
