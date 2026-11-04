# javax.imageio.stream.FileImageOutputStream — DESIGN

## 1. API 功能
`FileImageOutputStream` 是 `ImageOutputStreamImpl` 的具体子类，直接包装 `RandomAccessFile`（`"rw"`），对文件提供随机访问读写。方法分组：
- 构造：`FileImageOutputStream(File f)`（不存在则创建空文件）、`FileImageOutputStream(RandomAccessFile raf)`。`f == null`/`raf == null` → 精确 `IllegalArgumentException`。
- 写：全部基元/数组/位写直通 RAF，按 `byteOrder` 展开；**不缓冲**，每次写即刻落盘。
- 读回：`read()` 族可回读已写内容。
- `length()`：返回 RAF 真实长度（写后立即反映，`File.length()` 与之同步）；已有文件不截断。
- 缓存状态矩阵：三个 `isCached*()` 全 false。
- flush 语义：`flush()`/`flushBefore(pos)` 只推进 `getFlushedPosition`（文件本身已在盘上，无底层流要推）；低于 flushedPos 的 seek 被拒。
- `close()` 同时关闭底层 RAF。

## 2. Java 用户如何使用
典型链：`new FileImageOutputStream(new File("out.png"))` → `setByteOrder` → 写头写体（`File.length()` 即时可见）→ `seek` 回填修正 → `flush()` 无实际 IO 但锁定 flushedPos → `close()`。或经 `ImageIO.createImageOutputStream(File)` / RAF SPI 获得。注意：已存在的文件**不截断**，旧尾部残留。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/FileImageOutputStream.java`：`write` 直通 `raf.write`，`seek` 直通 `raf.seek`，`length()` 返回 `raf.length()`；`flush()` 在此实现里只更新 flushedPos。
- `com/sun/imageio/plugins/jpeg/JFIFMarkerSegment.java` 等编码器：经 `ImageIO.createImageOutputStream`（File 场景即本类）写真实图像文件。
- `com/sun/imageio/spi/FileImageOutputStreamSpi.java`、`RAFImageOutputStreamSpi.java`：生产者。
- JDK-27 侧 `test/jdk/javax/imageio/stream/FileImageStreamPos.java`（@bug 5043343）：RAF 指针采纳契约（见下）。

## 4. 测试用例设计 + 易错规范细节（`FileImageOutputStreamTest` testDeep*）
- 构造矩阵（精确类）：`(File)null`/`(RandomAccessFile)null` IAE；不存在文件创建后 `length()==0`、位置 0、默认 BE、无缓存。
- length/File.length 同步：写 1 字节 → 两侧皆 1（未 close 即落盘）；写满 7 字节布局逐位断言（`01 55 02 03 04 05 06 07` 家族）。
- 已有文件不截断：3 字节旧文件打开 `length()==3`、位置 0；覆写首字节后旧尾部原样（`09 07 07`）。
- 洞填 0：`seek(5)` 不增长，写后 `length()==6` 且中间 4 字节读回全 0（RAF 语义）。
- 写基元全类型 41 字节精确字节布局断言（`writeUTF("hi")` 为 4 字节修正 UTF）；LE 布局 32 字节逐位断言 + 经 `FileImageInputStream` 读回 round-trip（int/long/short/double/float/char/UTF）。
- 数组写：6 个数组 API 共 58 字节，`readFully` 各数组精确值。
- seek 读回/覆写：`readInt`/`readUnsignedShort` 混合；覆写中间字节后其余字节原样（半 int `0x3344` + 尾 short `0x5566`）；`seek` 复位 bitOffset。
- 位写：3 bit → `0xA0`；`0xAB 0x81`；`writeBits(x,0)` no-op；numBits 65/-1 精确 IAE；64 位全 1 跨 8 字节；RMW `0xFF` 位 2 起 → `0xCF`。
- 边界负例（精确类）：`write(null,0,1)` NPE、`write(b,0,-1)` IOOBE、`writeShorts(null,...)` NPE、off/len 越界 IOOBE、`read(null,...)` NPE、`read(b,0,0)` 返回 0。
- EOF 读：剩 1/2/3 字节时 `readShort/readInt/readLong` 精确 `EOFException`；`readFully` 部分填充后抛出并保留已读字节断言；`readBit/readBits` EOF 同抛。
- flushBefore：初始 flushedPos 0；`flush()` 推进到当前位置；`flushBefore(>pos)`/回退 flushBefore/`seek(<flushedPos)` → 精确 IOOBE。
- close 状态：字节已持久（`File.length()==1`）；第二次 close → 精确 IOException("closed")；close 后 write/read/seek/getStreamPosition/flushBefore/readBit 精确 IOException；`length()` 返回 -1 不抛。
- RAF 构造器版本差异（关键）：JDK-8 实测**不采纳** RAF 指针（`raf.seek(2)` 后 `getStreamPosition()==0`，但写仍落在 RAF 指针 2 处 —— 位置计数与真实写位置脱钩）；JDK-27 已修复（5043343），`getStreamPosition()==2`。两侧各自精确断言。
- 临时文件 `imgio-streamimpl-FileImageOutputStream*` finally 删除。
- javap 双侧对照：公开 API 完全一致。

## 5. 未写入 Javadoc 的实测行为
- 已存在的文件不截断，旧尾部残留（覆写语义）。
- 写不缓冲：`File.length()` 未 close 即增长。
- RAF 构造器不采纳指针（JDK-8 版本差异，5043343 于 JDK-27 修复）；且位置计数与 RAF 指针脱钩期间写入仍按 RAF 指针落盘。
- `flush()` 无底层流可推，仅推进 flushedPos 锁定回写边界。
- `close()` 连带关闭调用方传入的 RAF。
- 第二次 `close()` 抛 IOException("closed")（与 MemoryCache/FileCache 输出侧的 NPE 不同）。
