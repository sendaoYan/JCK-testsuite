# javax.imageio.stream.FileCacheImageOutputStream — DESIGN

## 1. API 功能
`FileCacheImageOutputStream` 是 `ImageOutputStreamImpl` 的具体子类，把写出内容暂存到临时文件，`flush()` 时把新增前缀推给底层 `OutputStream`。方法分组：
- 构造：`FileCacheImageOutputStream(OutputStream stream, File cacheDir)`；null stream → 精确 `IllegalArgumentException`("stream == null!")；cacheDir 非目录 → 精确 IAE("Not a directory!")；无权限 → 精确 `AccessDeniedException`；cacheDir=null 合法。
- 写：全部基元/数组/位写经基类展开到文件缓存；seek 越过长度洞填 0。
- 读回：`read()` 族可回读缓存。
- 缓存状态矩阵：`isCached()`=true、`isCachedMemory()`=false、`isCachedFile()`=true。
- flush 语义：`flush()` 只推送上次 flush 之后新增的字节给底层流（增量），`getFlushedPosition` 推进；`close()` 前未 flush 的尾部随 close 推出。
- `length()` 为缓存长度，随写增长。

## 2. Java 用户如何使用
典型链：`new FileCacheImageOutputStream(socketOut, null)` 或 `ImageIO.setUseCache(true)` + `ImageIO.createImageOutputStream(OutputStream)` → 写大头数据（临时落盘不占堆）→ `flush()` 增量推送（流式场景边写边发）→ `close()`。注意 flush 是增量而非全量重发。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/FileCacheImageOutputStream.java`：内部 `RandomAccessFile` 缓存；`flush()` 记录 `flushLen`，仅推送 `[lastFlushLen, max(streamPos, flushLen))` 增量。
- `com/sun/imageio/spi/OutputStreamImageOutputStreamSpi.java`：`useCache==true` 时 `createImageOutputStream` 返回本类。
- `jdk/test/javax/imageio/stream/StreamFlush.java`：上游对增量 flush/flushBefore 的验证（JDK-27 侧同在）。
- `jdk/test/javax/imageio/stream/MemoryCacheImageOutputStreamTest.java`：上游对照实现的行为参考。

## 4. 测试用例设计 + 易错规范细节（`FileCacheImageOutputStreamTest` testDeep*）
- 构造矩阵（精确类）：null stream IAE；cacheDir 非目录 IAE；无权限 `AccessDeniedException`；cacheDir=null 合法。
- 状态矩阵：`isCached/!isCachedMemory/isCachedFile`、默认 BE、`length()==0`。
- 增量 flush 生命周期（核心）：写 4 字节 flush（底层收 4）→ 追加写后 flush（底层只收新增 5..）→ 写 3 字节 flushBefore —— 对底层 `ByteArrayOutputStream` 字节逐位断言，含 UTF 长度标记字节（`00 01`、`00 05`、`68`）。`getFlushedPosition` 每步精确推进。
- LE 数组 round-trip：int/long/short/double/float/char/UTF 共 28 字节经同流 seek 回读 + `FileCacheImageInputStream` 交叉验证。
- seek/洞填 0：seek 越长不增长，写后中间 0；覆写不增长。
- 位写：`0xA0`（3 bit+flush）、`0xAB 0x81`（3+6+7 bits）、RMW `0xCF`；numBits 65/-1 精确 IAE。
- 边界负例（精确类）：null 数组 NPE；off/len 越界 IOOBE。
- EOF 读：`readShort` 剩 1 字节精确 `EOFException`；`read()` 到长度 -1。
- close 状态：第二次 close → 精确 `NullPointerException`（本类特有）；close 后 write/read/seek/getStreamPosition/flushBefore 精确 IOException；`length()` close 后 -1 不抛。
- 临时文件 `imgio-streamimpl-FileCacheImageOutputStream*` 独立命名，finally 递归删除（双重 close 抛 NPE，故 finally 用吞异常的 close）。
- 版本差异：JDK-8 与 JDK-27 公开 API（javap 对照）完全一致，行为一致。

## 5. 未写入 Javadoc 的实测行为
- `flush()` 是增量推送：只发送自上次 flush 后新增的前缀，Javadoc 未明说"增量"语义。
- 第二次 `close()` 抛 NPE（与 MemoryCacheImageOutputStream 同因：`stream` 被置 null）。
- `length()` close 后 -1 不抛。
- 无权限 cacheDir 抛 NIO `AccessDeniedException`。
- null 数组参数在越界检查前抛 NPE。
