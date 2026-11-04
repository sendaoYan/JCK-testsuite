# javax.imageio.stream.FileCacheImageOutputStream — DESIGN

## 1. API 功能
`FileCacheImageOutputStream` 是 `ImageOutputStreamImpl` 的具体子类，写入先进入磁盘缓存文件（`RandomAccessFile`），在 `flushBefore(pos)` 或 `close()` 时把前缀字节刷入底层 `OutputStream`。方法分组：
- 构造：`FileCacheImageOutputStream(OutputStream stream, File cacheDir)`。
- 写：`write(int)`、`write(byte[],int,int)`（继承 `ImageOutputStreamImpl` 的 `writeByte/writeInt/writeUTF/...`）。
- 读/回退：`read()`、`read(byte[],int,int)`、`seek(long)`、`length()`（返回缓存长度）。
- 缓存状态：`isCached()`（true）、`isCachedFile()`（true）、`isCachedMemory()`（false）。
- 丢弃：`flushBefore(long)` 把 `[flushedPos,pos)` 写入底层流；`close()` 刷全部并删除缓存文件。

## 2. Java 用户如何使用
典型链：`new FileCacheImageOutputStream(new ByteArrayOutputStream(), cacheDir)` → `writeByte/writeInt/writeUTF` 写 → `seek(0)` + `readByte/readInt/readUTF` 读回 → `flushBefore(pos)` 部分落盘 + `getFlushedPosition()` → `close()` 全部落盘，再从 `ByteArrayOutputStream.toByteArray()` 取最终字节。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/FileCacheImageOutputStream.java`：`flushBefore` 把 `flushBytes` 分 512 字节块 `cache.readFully` 后 `stream.write`；`close()` 先 `seek(maxStreamPos)` 再 `flushBefore(maxStreamPos)` 后删除缓存文件。
- `src/java.desktop/share/classes/com/sun/imageio/spi/OutputStreamImageOutputStreamSpi.java`：`ImageIO.createImageOutputStream(OutputStream)` 在 `useCache == true` 时返回 `new FileCacheImageOutputStream(os, cacheDir)`。
- `test/jdk/javax/imageio/stream/FlushBefore.java`：用 `new FileCacheImageOutputStream(new ByteArrayOutputStream(), null)` 验证 `flushBefore` 位置语义。

## 4. 测试用例设计 + 自动化实现（`FileCacheImageOutputStreamTest.testBlackBox`）
- 固定 `ByteArrayOutputStream` + `Files.createTempDirectory` 缓存目录，断言 `isCached/isCachedFile/!isCachedMemory`。
- 写 `writeByte/writeInt/writeBoolean/writeUTF/writeShort`，断言 `length()==15`、`getStreamPosition()==15`。
- `mark/reset` 回退；`seek(0)` 后逐个读回 `readByte/readInt/readBoolean/readUTF/readShort`。
- `seek(5)` + `flushBefore(5)`，断言 `getFlushedPosition()==5`、底层已收到 5 字节且内容正确、`seek(4)` 抛 `IndexOutOfBoundsException`。
- `close()` 后断言底层流共 15 字节；finally 递归删除缓存目录。

## 5. 深化补充（testDeep* 契约覆盖，与 JDK-8 侧同源）
追加 8 个 testDeep 方法（~100 条断言）：构造矩阵（null stream IAE、非目录 IAE、无权限 `AccessDeniedException`、cacheDir=null 合法）；状态矩阵；**增量 flush 生命周期**（4/9/12 字节三阶段对底层流逐位断言，含 UTF 标记字节）；LE 数组 28 字节 round-trip；seek 洞填 0；writeBits（0xA0、0xAB 0x81、RMW 0xCF、numBits IAE）；边界负例精确类；EOF readShort 精确 EOFException；close 状态（二次 close → 精确 `NullPointerException`，本类特有；length() 返回 -1 不抛）。临时文件 finally 递归删除。JDK-27 与 JDK-8 行为一致。
