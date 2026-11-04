# java.awt.image.BufferedImageOp 设计案

## 1. API 功能分组
单图像操作接口，5 个方法：`filter(BufferedImage,BufferedImage)`、`getBounds2D(BufferedImage)`、`createCompatibleDestImage(BufferedImage,ColorModel)`、`getPoint2D(Point2D,Point2D)`、`getRenderingHints()`。

## 2. Java 用户用法
实现类（RescaleOp/LookupOp/ConvolveOp/AffineTransformOp/ColorConvertOp）都实现本接口。典型链路：`BufferedImage dst = op.filter(src, null)`；`Rectangle2D b = op.getBounds2D(src)`；`BufferedImage compat = op.createCompatibleDestImage(src, src.getColorModel())`。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `java.awt.image` 各 Op 类实现该接口，`BufferedImage.filter(BufferedImageOp)` 委托调用。
- `test/jdk/java/awt/image/ConvolveOp/`、`LookupOp/`、`RescaleOp/` 通过接口方法 `filter/createCompatibleDestImage/getBounds2D` 驱动。
- 第三方图像库（如 ImageJ）以 `BufferedImageOp` 作为可插拔过滤器抽象。

## 4. 测试设计（指向 BufferedImageOpTest.java）
- 白盒：interface、public、5 个方法签名存在。
- 黑盒：用 RescaleOp 作为实现，走 `filter/getBounds2D/getPoint2D/createCompatibleDestImage/getRenderingHints` 契约；断言 identity rescale 下像素不变量。
