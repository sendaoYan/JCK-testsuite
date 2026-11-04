# javax.imageio.ImageIO — DESIGN

## 1. API 功能
`ImageIO` 是图像 I/O 的静态入口门面。方法分组：
- 插件查询：`getReaderFormatNames/getWriterFormatNames/getReaderMIMETypes/getWriterMIMETypes/getReaderFileSuffixes/getWriterFileSuffixes`。
- 实例发现：`getImageReaders(Object)`、`getImageReadersByFormatName/BySuffix/ByMIMEType`、`getImageWriters*`、`getImageWriter(reader)`、`getImageReader(writer)`、`getImageWriters(its,format)`、`getImageTranscoders(reader,writer)`。
- 流工厂：`createImageInputStream(Object)`、`createImageOutputStream(Object)`。
- 便捷读写：`read(File/InputStream/URL/ImageInputStream)`、`write(RenderedImage,String,File/OutputStream/ImageOutputStream)`。
- 缓存：`setUseCache/getUseCache/setCacheDirectory/getCacheDirectory/scanForPlugins`。

## 2. Java 用户如何使用
最常用组合链：`BufferedImage bi = ImageIO.read(new File("a.png"));` 与 `ImageIO.write(bi,"png",new File("out.png"));`。需要更多控制时：`Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("png"); ImageReader r = it.next(); r.setInput(ImageIO.createImageInputStream(in)); r.read(0);`。

## 3. 开源代码实际用法调研（来源：test/jdk）
- `test/jdk/javax/imageio/ImageIOWriteNull.java`：`ImageIO.write(null,null,(File)null)` 抛 `IllegalArgumentException`。
- `test/jdk/javax/imageio/EmptyFileTest.java`：不支持的格式 `ImageIO.write(img,"MY_IMG",out)` 返回 `false` 且不创建文件。
- `test/jdk/javax/imageio/ImageReaderReadAll.java`：`MemoryCacheImageInputStream(new ByteArrayInputStream(ba))` 作为 `setInput` 输入。

## 4. 测试用例设计 + 自动化实现（`ImageIOTest.testBlackBox`）
- 插件清单：reader/writer 格式名含 "png"、MIME/后缀非空。
- 内存内 png 往返：写 4x4 ARGB、读回尺寸与像素。
- 空流 read 返回 null。
- 按格式名取 reader/writer、`getImageReaders(流)` 发现 reader。
- `getImageTranscoders` 返回非 null 迭代器（可为空）。
- `createImageInputStream/createImageOutputStream` 内存流非 null。
- 写 `ImageOutputStream` 往返。
- 白盒：final、无公共构造器、关键静态方法存在。

## 5. 深化补充（第 2 轮，全部实测）
- 插件清单：格式名同时含大小写两种拼写（png/PNG、tiff/TIFF/tif/TIF、jpg/JPG 等）；MIME 含 image/tiff（JDK-9+ 内置 TIFF）；四个清单数组均为防御性拷贝（改元素不污染下次取值）。
- reader 发现：PNG 魔数 8 字节流 `getImageReaders` 非空且首个 spi `canDecodeInput` true；垃圾流空；`getImageReaders(null)`/`getImageReadersByFormatName(null)` → IAE；迭代器 remove → UnsupportedOperationException；空迭代器 next → NoSuchElementException。
- writer 能力矩阵实测（getImageWriters(its,format)）：jpg 拒绝 alpha 与 USHORT_GRAY（JDK-9+ 起 ARGB 为 false，JDK-8 为 true——版本差异）；bmp 拒绝 alpha/USHORT_GRAY；png/gif/tiff 全 true；未知 format 空。
- round-trip 矩阵：PNG 13 type 像素严格相等（回读 type：RGB→5、ARGB/ABGR→6、565/555/BGR→5、GRAY→10、UGRAY→11、BINARY→12、INDEXED→13）；BMP 8 type 像素相等（565 回读 8、555 回读 9），alpha/UGRAY 返回 false；GIF 全 type 可写但量化有损（断尺寸/type）；JPEG 无 alpha 类型有损（断尺寸）；**TIFF（JDK-9+）13 type 全部像素严格相等**（彩色类回读 TYPE_CUSTOM=0）。
- 负例精确异常（实测）：write(null img/format/output) IAE；未知 format 返回 false；写缺父目录 File → IIOException("Can't create an ImageOutputStream!")；read 4 重载 null → IAE；read(不存在 File) → IIOException("Can't read input file!")；read(空流/垃圾流) → null。
- 流关闭语义（实测）：read(ImageInputStream) 关流；read(InputStream) 不关底层流。
- 缓存/工厂（实测）：setCacheDirectory(普通文件/不存在目录) IAE("Not a directory!") 且失败不改状态；null 重置；createImageInputStream(null)/createImageOutputStream(null) → **IAE**（javadoc 暗示 null，实测抛）；useCache 决定 MemoryCache*/FileCache* 实现；File → FileImage*Stream。
- 大小写不敏感：PNG/Png/GIF/JPG 查找与写入全通过；scanForPlugins 安全；read(file:URL) 像素相等。
