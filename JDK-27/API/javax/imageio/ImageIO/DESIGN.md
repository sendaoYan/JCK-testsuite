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
