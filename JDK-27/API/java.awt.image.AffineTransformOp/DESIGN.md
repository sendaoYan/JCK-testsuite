# java.awt.image.AffineTransformOp 设计案

## 1. API 功能分组
- 常量：`TYPE_NEAREST_NEIGHBOR/TYPE_BILINEAR/TYPE_BICUBIC`。
- 构造：`AffineTransformOp(AffineTransform,RenderingHints)`、`AffineTransformOp(AffineTransform,int)`。
- 操作：`filter(BufferedImage,BufferedImage)`、`filter(Raster,WritableRaster)`。
- 查询：`getInterpolationType/getTransform/getBounds2D/getPoint2D/getRenderingHints/createCompatibleDestImage/createCompatibleDestRaster`。

## 2. Java 用户用法
- 几何变换：`new AffineTransformOp(AffineTransform.getScaleInstance(2,2), TYPE_BILINEAR).filter(src,null)`。
- 恒等变换 + 最近邻 = 精确复制；`getTransform()` 返回所用变换。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/TransformImage.java` 用 AffineTransformOp 变换图像。
- JDK 内部 `BufferedImage.filter` 对 AffineTransformOp 走几何变换路径。
- 图像库用最近邻/双线性缩放旋转图像。

## 4. 测试设计（指向 AffineTransformOpTest.java）
- 白盒：implements BufferedImageOp/RasterOp、常量与方法存在。
- 黑盒：常量值；恒等最近邻精确复制多像素；`getTransform/getInterpolationType/getRenderingHints`；scale(2) 的 `getPoint2D` 映射与 `getBounds2D` 尺寸；`createCompatibleDestImage`。
