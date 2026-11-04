# javax.imageio.ImageTypeSpecifier — DESIGN

## 1. API 功能
`ImageTypeSpecifier` 描述图像的 `ColorModel` + `SampleModel` 组合，供 reader/writer 协商目标格式。方法分组：
- 构造：`ImageTypeSpecifier(ColorModel, SampleModel)`、`ImageTypeSpecifier(RenderedImage)`。
- 静态工厂：`createPacked/createInterleaved/createBanded/createGrayscale/createIndexed/createFromBufferedImageType/createFromRenderedImage`。
- 查询：`getBufferedImageType`/`getNumComponents`/`getNumBands`/`getBitsPerBand(int)`/`getSampleModel()`/`getSampleModel(int,int)`/`getColorModel`。
- 生成：`createBufferedImage(int,int)`；以及 `equals`/`hashCode`。

## 2. Java 用户如何使用
`ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB); BufferedImage out = its.createBufferedImage(w,h);` 或 `writer.getDefaultImageMetadata(its, param)` 生成目标元数据。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param)` 接收该类型。
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：`getImageWriters(ImageTypeSpecifier, String)` 按目标类型筛选 writer。

## 4. 测试用例设计 + 自动化实现（`ImageTypeSpecifierTest.testBlackBox`）
- `createFromBufferedImageType(INT_RGB)`：类型/分量/波段/位深/样本模型/颜色模型/createBufferedImage。
- `createFromRenderedImage(ARGB)`：4 分量 4 波段、createBufferedImage 类型。
- `(ColorModel,SampleModel)` 构造引用身份与波段/分量数。
- `(RenderedImage)` 构造与 createFromRenderedImage 相等。
- `getSampleModel(2,3)` 尺寸调整。
- `createGrayscale(8,BYTE,false)` 分量/波段/位深/BYTE_GRAY 类型。
- equals/hashCode：同布局相等、null 不相等、跨类型不相等。
- 白盒：两个公共构造器 + 全部工厂/查询方法存在。

## 5. 深化补充（第 2 轮，全部实测）
- 13 种 TYPE_* 全矩阵（comps/bands/bitsPerBand 精确值表 + createFromBufferedImageType/createFromRenderedImage/createBufferedImage 三重 type 往返，13/13）。
- 工厂正例矩阵：createPacked 带 alpha → TYPE_INT_ARGB（各 band 8 bits、premult 进 ColorModel、无 alpha → TYPE_INT_RGB）；createInterleaved/createBanded → TYPE_CUSTOM；createGrayscale 1bit→BYTE_BINARY、8bit→BYTE_GRAY、16bit USHORT→USHORT_GRAY，**4 参重载总是带 alpha**（第 4 参仅为 isAlphaPremultiplied）；createIndexed 1/4bit→BYTE_BINARY、8bit→BYTE_INDEXED、null alpha LUT → 3 分量无 alpha。
- 负例全表（实测均 IAE）：详见测试 testFactoryNegatives（bits 非 1/2/4/8/16、dataType 非法、LUT 长度≠2^bits、mask 不连续、null 参数、getBitsPerBand 越界、getSampleModel/createBufferedImage 非正尺寸）。
- equals/hashCode：跨实例、跨工厂种类一致；**版本差异**：`createFromRenderedImage(TYPE_BYTE_INDEXED)` 与 canonical 调色板在本 JDK equals=false（JDK-9+ 改了默认调色板；JDK-8 为 true），type 往返仍成立。
