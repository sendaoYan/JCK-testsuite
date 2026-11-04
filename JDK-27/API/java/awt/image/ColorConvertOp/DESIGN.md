# java.awt.image.ColorConvertOp 设计案

## 1. API 功能分组
- 构造：`ColorConvertOp(RenderingHints)`、`ColorConvertOp(ColorSpace,hints)`、`ColorConvertOp(ColorSpace,ColorSpace,hints)`、`ColorConvertOp(ICC_Profile[],hints)`。
- 操作：`filter(BufferedImage,BufferedImage)`、`filter(Raster,WritableRaster)`。
- 查询：`getICC_Profiles/getBounds2D/getPoint2D/getRenderingHints/createCompatibleDestImage/createCompatibleDestRaster`。

## 2. Java 用户用法
- 颜色空间转换：`new ColorConvertOp(sRGB, CS_LINEAR_RGB, null).filter(src, null)`。
- 往返：`toLin.filter(src)` 再 `toSrgb.filter(mid)` 近似还原（容差 ±2）。
- `getICC_Profiles()` 取转换涉及的 ICC profile。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `ConvolveOp.filter` 需要色彩空间调整时内建 `ColorConvertOp`。
- `test/jdk/java/awt/image/` 下 `ImagingOpsNoExceptionsTest`、`GetSamplesTest` 间接触发色彩转换。
- 图像库用 ColorConvertOp 做 RGB↔CMYK/Gray 转换。

## 4. 测试设计（指向 ColorConvertOpTest.java）
- 白盒：implements BufferedImageOp/RasterOp、关键方法存在。
- 黑盒：sRGB→线性 RGB→sRGB 往返，5 种颜色各通道容差 ±2；`getBounds2D/getPoint2D/createCompatibleDestImage/getRenderingHints`；ICC_Profile[] 构造 `getICC_Profiles` 长度 1。
