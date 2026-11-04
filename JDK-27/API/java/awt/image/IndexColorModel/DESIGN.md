# java.awt.image.IndexColorModel 设计案

## 1. API 功能分组
- 构造：`IndexColorModel(int bits,int size,byte[] r,g,b[,a|trans])`、`IndexColorModel(int,int,int[],int,boolean,int,int)`、`BigInteger` 变体。
- 查表查询：`getMapSize/getTransparentPixel/getReds/getGreens/getBlues/getAlphas/getRGBs/getRed/getGreen/getBlue/getAlpha/getRGB`。
- 索引/组件：`getDataElement/getDataElements/getComponents/isValid/isValid(int)/getValidPixels`。
- 兼容性：`createCompatibleWritableRaster/createCompatibleSampleModel/isCompatibleRaster/isCompatibleSampleModel/convertToIntDiscrete`。

## 2. Java 用户用法
- 调色板图像：`new IndexColorModel(8, n, reds, greens, blues)`；`getRGB(i)` 得到 `0xFFRRGGBB`。
- 带透明索引：`new IndexColorModel(8, n, r, g, b, transparentIndex)`，`getTransparentPixel()` 返回该索引。
- 索引像素：`getDataElement(int[] rgbComponents)` 反向查表得到最近索引。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/IndexColorModel/` 覆盖查表与透明索引。
- JDK 内部 GIF/位图解码（`com.sun.imageio.plugins.gif`）用 `IndexColorModel` 构造调色板图像。
- `BufferedImage(TYPE_BYTE_INDEXED)` 与 `getRGB` 转真彩时依赖 `convertToIntDiscrete`。

## 4. 测试设计（指向 IndexColorModelTest.java）
- 白盒：extends ColorModel、查表方法存在。
- 黑盒：8 位 4 色无 alpha 模型，`getMapSize/getPixelSize/getNumComponents/getRed/Green/Blue/getRGB` 精确；`getReds/Greens/Blues/getRGBs`；含 alpha 模型 `getAlpha/getRGB`；透明像素构造 `getTransparentPixel/BITMASK`；`getDataElement` 反向查表；`createCompatibleWritableRaster` 1 波段；`isValid/getValidPixels`。
