# javax.imageio.ImageTypeSpecifier — DESIGN（JDK-8）

## 1. API 功能
`ImageTypeSpecifier`（不可变值类，**不实现 Serializable**——javap 实测无 implements 子句）描述 ColorModel+SampleModel 组合，供 reader/writer 协商目标类型。方法：
- 构造：`(ColorModel, SampleModel)`（null → IAE）、`(RenderedImage)`（取其 cm/sm，null → IAE）。
- 静态工厂：`createPacked(ColorSpace,4 masks,transferType,premult)`、`createInterleaved(ColorSpace,bandOffsets,dataType,hasAlpha,premult)`、`createBanded(ColorSpace,bankIndices,bandOffsets,dataType,hasAlpha,premult)`、`createGrayscale(bits,dataType,signed)` 与 4 参 `(...,isAlphaPremultiplied)`、`createIndexed(r,g,b,a LUTs,bits,dataType)`、`createFromBufferedImageType(int)`、`createFromRenderedImage(RenderedImage)`。
- 查询：`getBufferedImageType/getNumComponents/getNumBands/getBitsPerBand(int)/getSampleModel()/getSampleModel(w,h)/getColorModel/createBufferedImage(w,h)` + `equals/hashCode`（按 cm/sm 值比较）。

## 2. Java 用户如何使用
`ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB).createBufferedImage(w,h)` 造目标图；`ImageIO.getImageWriters(its, "png")` 按类型筛 writer；`writer.getDefaultImageMetadata(its, param)` 生成目标元数据。

## 3. 开源代码实际用法调研（来源：~/git/jdk 系源码与 test/jdk，仅借鉴思路）
- `test/jdk/javax/imageio/ImageTypeSpecifierTest.java`：构造器/工厂的异常矩阵（IAE）。
- `test/jdk/javax/imageio/ImageTypeSpecifierBitsPerBand.java`：`getBitsPerBand` 越界 → IllegalArgumentException。
- `test/jdk/javax/imageio/ITSDataType.java`：dataType 合法值矩阵。
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：`getImageWriters(type, format)` 用 spi `canEncodeImage(type)` 过滤。
- `src/java.desktop/share/classes/javax/imageio/ImageTypeSpecifier.java`：`createGrayscale` 4 参重载内部 `Grayscale(..., true, isAlphaPremultiplied)` —— **总是带 alpha**。

## 4. 测试用例设计 + 易错规范细节（`ImageTypeSpecifierTest`）
- 13 种 `BufferedImage.TYPE_*` 全矩阵（实测值表）：components {RGB3,ARGB4,ARGB_PRE4,BGR3,3BGR3,ABGR4,ABGR_PRE4,565:3,555:3,GRAY1,UGRAY1,BINARY3,INDEXED3}、bands（BINARY/INDEXED 为 1）、bitsPerBand（565/555 为 5、UGRAY 16、BINARY 1，其余 8）。每种断言 type 往返 + `createFromRenderedImage` 与 `createFromBufferedImageType` equals + hashCode 一致 + `createBufferedImage` type 往返（13/13，实测）。
- 工厂正例（实测）：createPacked 带 alpha → TYPE_INT_ARGB、各 band 8 bits、premult 标志进 ColorModel；无 alpha → 3 bands、createBufferedImage 为 TYPE_INT_RGB；createInterleaved/createBanded → TYPE_CUSTOM（0）、hasAlpha 加 band；createGrayscale 1bit→BYTE_BINARY、8bit→BYTE_GRAY、16bit USHORT→USHORT_GRAY、4 参**总是带 alpha**（第 4 参只是 isAlphaPremultiplied，实测双侧一致）；createIndexed 1/4bit→BYTE_BINARY、8bit→BYTE_INDEXED、null alpha LUT → 无 alpha。
- 负例全部实测为 IAE（精确类）：ctor null cm/sm、RenderedImage null；createFromBufferedImageType(-1/999) "Invalid BufferedImage type!"、TYPE_CUSTOM "Cannot create from TYPE_CUSTOM!"；createPacked null cs / 不连续 mask "Noncontiguous red mask"；createInterleaved null offsets/null cs/bank 数不匹配；createBanded null banks/长度不匹配；createGrayscale bits 非 1/2/4/8/16（0/12/17）"Bad value for bits!"、dataType 非法 "Bad value for dataType!"、16bit+BYTE "Too many bits for dataType!"；createIndexed LUT 长度≠2^bits "LUT has improper length!"、bits 3/0 "Bad value for bits!"、bits 16 "Too many bits..."、null LUT "LUT is null!"；getBitsPerBand 越界/负 "band out of range!"；getSampleModel/createBufferedImage 0 或负尺寸 "Width (x) and height (y) must be > 0"。
- equals/hashCode：跨实例（不同尺寸同 type 的两图）相等且 hash 一致；fromBIType vs fromImage 相等；反射性/null/异类型；TYPE_3BYTE_BGR ≠ TYPE_INT_BGR ≠ RGB 族；grayscale ≠ opaque indexed。**版本差异**：`createFromRenderedImage(TYPE_BYTE_INDEXED 图)` 在 JDK-8 与 canonical equals=true，JDK-9+ 因默认调色板改变为 false（双侧各自断言并注明）。
- 易错点：JDK-8 侧 `getBitsPerBand(3)` 等越界抛 IAE 而非 AIOOBE；createPacked 的 mask 合法性在构造时校验（不连续 mask 立即 IAE）。
