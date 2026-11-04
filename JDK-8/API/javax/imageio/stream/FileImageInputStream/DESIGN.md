# javax.imageio.stream.FileImageInputStream — DESIGN

## 1. API 功能
`FileImageInputStream` 是 `ImageInputStreamImpl` 的具体子类，直接包装 `RandomAccessFile`，对文件提供零拷贝随机访问读。方法分组：
- 构造：`FileImageInputStream(File f)`（内部 `new RandomAccessFile(f, "r")`）、`FileImageInputStream(RandomAccessFile raf)`。`f == null`/`raf == null` → 精确 `IllegalArgumentException`；文件不存在 → 精确 `FileNotFoundException`；`f` 是目录 → 精确 `FileNotFoundException`。
- 读：`read()`、`read(byte[],int,int)` 直通 RAF；全部继承基元读按 `byteOrder` 组装。
- `length()`：返回 RAF 真实长度（文件边界精确；文件为空返回 0）。
- 缓存状态矩阵：`isCached()/isCachedMemory()/isCachedFile()` 全 false。
- `close()` 同时关闭底层 RAF（之后对 RAF 的读抛 `IOException`）。

## 2. Java 用户如何使用
典型链：`new FileImageInputStream(new File("img.png"))` → `length()` 精确文件长 → seek 到任意偏移读头/像素 → `close()`。或经 `ImageIO.createImageInputStream(File)` / `RAFImageInputStreamSpi`（传 `RandomAccessFile` 时）自动获得本类。读大图首选（无缓存副本）。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/FileImageInputStream.java`：`read()` 直通 `raf.read()` 并维护 `streamPos` 计数；`length()` 返回 `raf.length()`；`close()` 关 RAF。
- `com/sun/imageio/spi/FileImageInputStreamSpi.java`、`RAFImageInputStreamSpi.java`：`ImageIO.createImageInputStream(File/RandomAccessFile)` 的生产者。
- `jdk/test/javax/imageio/ReadAsGrayTest.java`、`ImageStreamFromRAF.java` 等：经 `ImageIO` 间接消费本类。
- JDK-27 侧 `test/jdk/javax/imageio/stream/FileImageStreamPos.java`（@bug 5043343）：验证 FIIS/FIOS 构造器应采纳 RAF 指针 —— 即 JDK-27 修复了本 JDK-8 版本仍存在的 streamPos 不采纳问题。

## 4. 测试用例设计 + 易错规范细节（`FileImageInputStreamTest` testDeep*）
- 构造矩阵（精确类）：`(File)null`/`(RandomAccessFile)null` → IAE；不存在文件 → `FileNotFoundException`；目录 → `FileNotFoundException`。
- length 语义：普通文件精确字节长；空文件 0；seek 不改变 length；close 后 `length()` 返回 -1 不抛。
- 缓存状态矩阵：三个 isCached 全 false。
- 基元读 BE/LE 双字节序全类型精确值 + 数组 `readFully` round-trip。
- EOF 精度：剩 1 字节 `readShort`、剩 2 字节 `readInt`、剩 3 字节 `readLong` → 全部精确 `EOFException`；`read()`/`read(byte[])` 到尾 -1；`readBit/readBits` EOF 同抛。
- mark/reset 嵌套；无 mark `reset` → IOException；`reset` 入 flushed 区 → 精确 `javax.imageio.IIOException`；`flushBefore` 后 `seek(<flushedPos)` IOOBE。
- skipBytes：返回值、跨 EOF 返回 n、负数 IOOBE。
- 位操作：`readBit` MSB、`readBits` 跨字节回卷、`setBitOffset` 合法 0..7/非法 -1,8 IAE、`seek` 复位 bitOffset。
- close 语义：第二次 close → 精确 IOException("closed")；close 后各操作精确 IOException；**close 关闭底层 RAF**（RAF 再读抛 IOException 实测断言）。
- RAF 构造器版本差异（关键）：JDK-8 实测构造器**不采纳** RAF 指针 —— `raf.seek(4)` 后 `new FileImageInputStream(raf).getStreamPosition()==0`（读仍从 RAF 指针处开始，流位置计数从 0 起）；JDK-27 已修复（5043343），`getStreamPosition()==4`。两侧各自精确断言。
- 临时文件 `imgio-streamimpl-FileImageInputStream*` finally 删除。
- javap 双侧对照：公开 API 完全一致。

## 5. 未写入 Javadoc 的实测行为
- RAF 构造器不采纳文件指针（JDK-8）：`getStreamPosition()` 与真实读位置脱钩（版本差异，JDK-27 修复，bug 5043343）。
- 目录路径抛 `FileNotFoundException` 而非 IAE。
- `close()` 连带关闭调用方传入的 RAF（所有权转移未写进 Javadoc）。
- `length()` close 后 -1 不抛。
- reset 入 flushed 区抛 `javax.imageio.IIOException` 精确类。
