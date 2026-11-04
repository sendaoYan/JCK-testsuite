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

## 5. 深化补充（testDeep* 契约覆盖，含 JDK-27 特有断言）
追加 11 个 testDeep 方法（~130 条断言）：构造矩阵（(File)null/(RandomAccessFile)null IAE、不存在文件创建空文件）；length()/File.length() 未 close 即同步增长；已有文件**不截断**（旧尾部残留实测）；seek 越长洞填 0（55 00 00 00 00 AA）；全类型写 41 字节精确落盘布局；LE 32 字节布局 + 经 FileImageInputStream round-trip；数组 58 字节 round-trip；位写（0xA0、0xAB 0x81、RMW 0xCF、writeBits(x,0) no-op、numBits 65/-1 IAE、64 位满宽）；边界负例精确类（null 数组 NPE、len<0 IOOBE）；EOF 精度（readFully 部分填充保留断言）；flush()/flushBefore 仅推进 flushedPos（越界 IOOBE）；close 状态（二次 close → IOException("closed")、length() 返回 -1 不抛）。**版本差异（JDK-27 特有）**：RandomAccessFile 构造器采纳 RAF 指针（`raf.seek(2)` 后 `getStreamPosition()==2`；JDK-8 实测为 0，bug 5043343，上游回归测试 FileImageStreamPos.java）；两侧各自精确断言。javap 双侧公开 API 相同。
