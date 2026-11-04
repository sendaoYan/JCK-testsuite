# javax.imageio.stream.FileCacheImageInputStream — DESIGN

## 1. API 功能
`FileCacheImageInputStream` 是 `ImageInputStreamImpl` 的具体子类，将 `InputStream` 内容按需暂存到临时文件（文件缓存），在顺序输入流上提供随机访问。方法分组：
- 构造：`FileCacheImageInputStream(InputStream stream, File cacheDir)`。`stream == null` → 精确 `IllegalArgumentException`("stream == null!")；`cacheDir == null` 合法（用系统默认临时目录）；`cacheDir` 为普通文件 → 精确 `IllegalArgumentException`("Not a directory!")；`cacheDir` 无写权限 → 精确 `java.nio.file.AccessDeniedException`（NIO 异常，非 IOException 直系）。
- 读：`read()`、`read(byte[],int,int)` + 全部继承基元读。
- 缓存状态矩阵：`isCached()`=true、`isCachedMemory()`=false、`isCachedFile()`=true。
- 生命周期：缓存文件在构造时创建、`close()` 时删除（`DeleteOnExit` 兜底）。
- `length()` 恒 `-1L`（输入总长未知）。

## 2. Java 用户如何使用
典型链：`new FileCacheImageInputStream(new FileInputStream(img), cacheDir)` 或 `ImageIO.setUseCache(true)` 后 `ImageIO.createImageInputStream(InputStream)` 自动选择本类 → 随机 seek 读像素头 → `close()`（务必关闭，否则临时文件残留）。大图解码避免内存缓存占满堆。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/FileCacheImageInputStream.java`：构造 `File.createTempFile("jci", ".tmp", cacheDir)` 建 `RandomAccessFile` 缓存；`read()` 先 `cache.length()` 不足则从源流补写缓存再读；`close()` 删缓存文件。
- `com/sun/imageio/spi/InputStreamImageInputStreamSpi.java`：`useCache==true` 时 `createImageInputStream` 返回本类。
- `jdk/test/javax/imageio/stream/FileCacheImageInputStreamNullTest.java`：上游同样验证 null stream 的 IAE。
- `jdk/test/javax/imageio/stream/DeleteOnExitTest.java`：验证缓存文件被清理。
- `com/sun/imageio/stream/StreamFinalizer.java`（JDK-8 特有，JDK-27 已移除 finalizer）：兜底 close。

## 4. 测试用例设计 + 易错规范细节（`FileCacheImageInputStreamTest` testDeep*）
- 构造矩阵：null stream → 精确 IAE；cacheDir=null（默认临时目录）合法；普通文件作 cacheDir → 精确 IAE("Not a directory!")；无权限目录 → 精确 `AccessDeniedException`。
- 缓存文件生命周期：目录中条目数 1（缓存文件存在）→ close 后 0（被删除）。
- 缓存状态矩阵：`isCached/!isCachedMemory/isCachedFile`；默认 BE；`length()==-1`。
- 基元读 BE/LE 双字节序精确值；EOF 精度（剩 1 字节 `readShort` → 精确 `EOFException` 等）；边界负例（null 数组 NPE、越界 IOOBE、非法 seek/flushBefore IOOBE、`setBitOffset` IAE）。
- mark/reset 嵌套 + 无 mark reset IOException + reset 入 flushed 区 `javax.imageio.IIOException`。
- skipBytes 返回值语义；负数 IOOBE。
- 位操作跨字节；close 幂等（第二次 close → 精确 IOException("closed")）；close 后操作精确 IOException。
- 临时目录/文件统一 `java.io.tmpdir` 下 `imgio-streamimpl-FileCacheImageInputStream*` 独立命名，finally 递归删除。
- 版本差异：JDK-8 与 JDK-27 公开 API（javap 对照）完全一致；JDK-8 有 `StreamFinalizer` 兜底（包私有，不影响公开面），行为一致。

## 5. 未写入 Javadoc 的实测行为
- `cacheDir == null` 合法（Javadoc 只写"where the cache file is to be created"，未说明 null 语义）。
- 无权限目录抛 NIO 的 `AccessDeniedException`，早于任何 IOException 声明出现在构造器里。
- `length()` 恒 -1（同 MemoryCache 版）。
- `skipBytes` 越过 EOF 返回 n（纯 seek 语义）；负参数 IOOBE。
- reset 入 flushed 区抛 `javax.imageio.IIOException`（精确类断言区分于普通 IOException）。
