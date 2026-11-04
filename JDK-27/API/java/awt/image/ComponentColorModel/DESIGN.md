# java.awt.image.ComponentColorModel 设计案

## 1. API 功能分组
- 构造：`ComponentColorModel(ColorSpace,int[],boolean,boolean,int,int)`、`ComponentColorModel(ColorSpace,boolean,boolean,int,int)`。
- 像素/组件：`getRed/getGreen/getBlue/getAlpha/getRGB`（int 与 Object/byte[]）、`getDataElements/getComponents/getDataElement`、`getNormalizedComponents/getUnnormalizedComponents`。
- 兼容性：`createCompatibleWritableRaster/createCompatibleSampleModel/isCompatibleRaster/isCompatibleSampleModel/getAlphaRaster/coerceData`。

## 2. Java 用户用法
- 8 位 sRGB：`new ComponentColorModel(sRGB, new int[]{8,8,8}, false, false, OPAQUE, TYPE_BYTE)`。
- 用 `byte[]` 表示单像素：`getRGB(new byte[]{r,g,b})`；`getDataElements(0xFF445566, null)` 打包成 `byte[]`。
- 预乘 alpha 模型在读出时自动除以 alpha。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/ComponentColorModel/` 覆盖构造与转换。
- JDK 内部 `java.awt.image` 的 `BandedSampleModel/ComponentSampleModel` 配合该模型存储非打包像素。
- 图像库常用 `ComponentColorModel` 表示 CMYK/多波段分量图像。

## 4. 测试设计（指向 ComponentColorModelTest.java）
- 白盒：extends ColorModel、关键 Object 方法存在。
- 黑盒：sRGB 8 位无 alpha 模型，`getRGB(byte[])` 精确、`getDataElements` 打包/往返；兼容 raster/sampleModel 波段数=3；预乘模型 `getRed` 除以 alpha、`getAlpha` 回读。
