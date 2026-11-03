# javax.imageio.stream.FileCacheImageInputStream — DESIGN

## 1. API 功能
`FileCacheImageInputStream` 是 `ImageInputStreamImpl` 的具体子类，把 `InputStream` 的内容按需读入一个磁盘缓存文件（`RandomAccessFile`），从而在只支持顺序读的输入流上提供随机访问（seek/mark/reset/skipBytes）。方法分组：
- 构造：`FileCacheImageInputStream(InputStream stream, File cacheDir)`；`cacheDir == null` 时用系统临时目录，非目录抛 `IllegalArgumentException`。
- 读取：`read()`、`read(byte[],int,int)`（继承 `ImageInputStreamImpl` 的 `readByte/readInt/readFully/...` 也在此之上工作）。
- 缓存状态：`isCached()`（恒 true）、`isCachedFile()`（true）、`isCachedMemory()`（false）。
- 关闭：`close()` 关闭并删除缓存文件、把流从 `StreamCloser` 队列移除。
- 注意：该类不覆盖 `length()`，故 `length()` 恒为 `-1L`（长度未知）。

## 2. Java 用户如何使用
典型链：`new FileCacheImageInputStream(new ByteArrayInputStream(bytes), cacheDir)` → `isCached()` 确认缓存 → `read()`/`read(byte[],off,len)` 顺序读 → `seek(n)`/`skipBytes(n)` 随机跳 → `mark()` + `readFully(...)` + `reset()` 回退 → `flushBefore(pos)` + `getFlushedPosition()` 丢弃前缀 → `close()`。`cacheDir` 是目录，缓存文件在 `close()` 时自动删除。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/FileCacheImageInputStream.java`：`read()` 通过 `readUntil(streamPos+1)` 把源流读入 `RandomAccessFile cache`，再 `cache.seek(streamPos++)` 读出；`read(byte[],int,int)` 用 `cache.readFully`。
- `src/java.desktop/share/classes/com/sun/imageio/spi/InputStreamImageInputStreamSpi.java`：`ImageIO.createImageInputStream(InputStream)` 在 `useCache == true` 时返回 `new FileCacheImageInputStream(is, cacheDir)`（否则 `MemoryCacheImageInputStream`）。
- `test/jdk/javax/imageio/stream/FileCacheImageInputStreamNullTest.java`：验证传入 `null` stream 抛 `IllegalArgumentException`。

## 4. 测试用例设计 + 自动化实现（`FileCacheImageInputStreamTest.testBlackBox`）
- 用固定 24 字节数组 + `Files.createTempDirectory` 作缓存目录构造，断言 `isCached/isCachedFile/!isCachedMemory/length()==-1/初始位置 0`。
- 顺序读：`read()`、`read(buf,1,3)` 断言计数与值、`getStreamPosition` 跟踪。
- 随机读 + 回退：`mark()` → `readFully` → `reset()` 回到标记位；`seek(10)` + `readByte`；`skipBytes(3)`。
- 丢弃：`flushBefore(5)` + `getFlushedPosition()`，并对 `seek(4)` 断言 `IndexOutOfBoundsException`。
- finally 中 `close()` 并递归删除临时目录，避免跨平台残留。
