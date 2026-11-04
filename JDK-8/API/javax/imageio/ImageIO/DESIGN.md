# javax.imageio.ImageIO — DESIGN（JDK-8）

## 1. API 功能
`ImageIO` 是图像 I/O 的静态门面（final、无公共构造器）。方法分组：
- 插件清单：`getReaderFormatNames/getWriterFormatNames/getReaderMIMETypes/getWriterMIMETypes/getReaderFileSuffixes/getWriterFileSuffixes`，返回数组是防御性拷贝。
- 实例发现：`getImageReaders(Object)`、`getImageReadersByFormatName/BySuffix/ByMIMEType`、`getImageWritersByFormatName/BySuffix/ByMIMEType`、`getImageWriters(ImageTypeSpecifier,String)`、`getImageWriter(reader)`、`getImageReader(writer)`、`getImageTranscoders(reader,writer)`。
- 流工厂：`createImageInputStream(Object)`、`createImageOutputStream(Object)`；由 `useCache` 决定 FileCache/MemoryCache 实现。
- 便捷读写：`read(File/InputStream/URL/ImageInputStream)`、`write(RenderedImage,String,File/OutputStream/ImageOutputStream)`。
- 缓存与插件：`setUseCache/getUseCache/setCacheDirectory/getCacheDirectory/scanForPlugins`。

## 2. Java 用户如何使用
最常见组合：`ImageIO.read(new File("a.png"))` + `ImageIO.write(img,"png",file)`。需要控制过程时用 `getImageReadersByFormatName("png")` 拿 `ImageReader`，配 `ImageIO.createImageInputStream(...)` 喂给 `setInput`。格式名/后缀/MIME 查找大小写不敏感（"PNG"/"png" 等价）。

## 3. 开源代码实际用法调研（来源：~/git/jdk 系源码与 test/jdk，仅借鉴思路）
- `test/jdk/javax/imageio/GetReaderWriterInfo.java`：断言 `getReaderFormatNames()` 等六个清单方法非空。
- `test/jdk/javax/imageio/GetReaderWriterInfoNullTest.java`：空数组方法返回非 null。
- `test/jdk/javax/imageio/ImageIOWriteNull.java`、`WriteNullImageTest.java`、`NullInputOutput.java`：`write(null,..)`、`read(null)` 的 null 契约（`IllegalArgumentException`）。
- `test/jdk/javax/imageio/EmptyFileTest.java`：不支持的格式 `write` 返回 false 且不创建文件。
- `test/jdk/javax/imageio/ImageIOWriteFile.java`、`ImageIOGetImageReaders.java`：File 写、流发现 reader 的等价类。
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：`read(File)` 捕获 `RuntimeException` 后包装 `IIOException("Can't read input file!")`；null 检查用 `IllegalArgumentException("xxx == null!")`。

## 4. 测试用例设计 + 易错规范细节（`ImageIOTest`）
- 插件清单（实测 JDK-8）：格式名同时含小写与全大写（png/PNG、jpg/JPG、gif/GIF、wbmp/WBMP、jpeg/JPEG、bmp/BMP）；MIME 含 image/png、image/jpeg、image/gif、image/bmp；后缀含 png/jpg。JDK-8 无 TIFF。
- 防御性拷贝：改 `getReaderFormatNames()[0]="ZZZ"` 后再取不受污染；四个清单方法同测。
- reader 发现：PNG 魔数 8 字节流 → `getImageReaders` 非空且首个 spi `canDecodeInput` true；完整 PNG 流可报尺寸；垃圾流空；`getImageReaders(null)` IAE（非 NPE）；迭代器 `remove` → UnsupportedOperationException；空迭代器 `next` → NoSuchElementException。
- writer 能力矩阵（`getImageWriters(its,format)` 实测）：png 全 7 类 type true；jpg 除 USHORT_GRAY 外 true（注意：ARGB 被 spi 声称可写但 native 写抛 IIOException——本 JDK 特有）；bmp 拒绝 alpha 与 USHORT_GRAY；gif 全 true。
- round-trip 矩阵（write→read，逐像素 getRGB 相等）：
  - PNG：13 种 BufferedImage type 全部 write=true 且像素严格相等；回读 type 表 {RGB→5, ARGB/ABGR→6, 565/555/BGR→5, GRAY→10, UGRAY→11, BINARY→12, INDEXED→13}。
  - BMP：INT_RGB/INT_BGR/3BYTE_BGR/565(回读 8)/555(回读 9)/BYTE_GRAY/BYTE_BINARY/BYTE_INDEXED 像素相等；alpha 类与 USHORT_GRAY 返回 false 且流为空。
  - GIF：全部 type 可写（内部量化），读回 BYTE_INDEXED/BYTE_BINARY，有损仅断言尺寸/type。
  - JPEG：无 alpha 类 write=true，有损只断言尺寸/非 null；ARGB write 抛 `IIOException("Invalid argument to native writeImage")`（JDK-8 实测，JDK-9+ 起为干净 false）。
- write 负例（实测）：`write(null img/format/output)` → IAE("image == null!"/"formatName == null!"/"output == null!")；未知 format → 返回 false 不写字节；写缺父目录的 File → `IIOException("Can't create an ImageOutputStream!")`（cause 是 FileNotFoundException，SPI 内部 printStackTrace 到 stderr，无害）；未知 format 写 File 不创建文件。
- read 负例（实测）：4 个重载 null → IAE；不存在 File → `IIOException("Can't read input file!")`；空流/垃圾流 → 返回 null（不抛）；`read(ImageInputStream)` 关流（后续 read 抛 IOException "closed"）；`read(InputStream)` 不关底层流（close 计数探针）。
- 缓存：setUseCache 往返、setCacheDirectory(tmpdir) 引用往返、普通文件/不存在目录 → IAE("Not a directory!") 且失败后状态不变、null 重置；finally 恢复全局。
- 流工厂（实测）：null → IAE（javadoc 暗示返回 null，实际两侧 JDK 都抛 IAE，实测记录）；useCache=false → MemoryCacheImageInputStream/OutputStream，true → FileCache*；File → FileImageInputStream/OutputStream。
- `scanForPlugins()` 安全调用一次后 png reader/writer 仍在。
- File/URL 路径：write(File)→read(File) 像素相等；read(file: URL) 像素相等。
- 确定性：固定像素 pattern（坐标线性函数）、临时文件独立命名并 finally 删除、全局缓存状态 finally 恢复、不依赖编码字节级输出。
