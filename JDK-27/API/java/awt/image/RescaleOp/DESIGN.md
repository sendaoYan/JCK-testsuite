# java.awt.image.RescaleOp 设计案

## 1. API 功能分组
- 构造：`RescaleOp(float[],float[],RenderingHints)`、`RescaleOp(float,float,RenderingHints)`。
- 查询：`getScaleFactors/getOffsets/getNumFactors`。
- 操作：`filter(BufferedImage,BufferedImage)`、`filter(Raster,WritableRaster)`、`getBounds2D/getPoint2D/getRenderingHints/createCompatibleDestImage/createCompatibleDestRaster`。

## 2. Java 用户用法
- 亮度/对比度：`new RescaleOp(2.0f, 10.0f, null).filter(src,null)`，输出 `val = (int)(src*scale+offset)` 并裁剪 0–255。
- 按波段：`new RescaleOp(new float[]{1,2,3}, new float[]{0,0,0}, null)`。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/RescaleOp/` 覆盖缩放/偏移结果。
- JDK 内部 `BufferedImage.filter` 对 RescaleOp 走查找表或原生 rescale 路径。
- 图像库用 RescaleOp 做增益/偏置调整。

## 4. 测试设计（指向 RescaleOpTest.java）
- 白盒：implements BufferedImageOp/RasterOp、查询方法存在。
- 黑盒：单因子 `2.0/10.0` 输出 `(100,50,200)->(210,110,255)`；`0.5/0` 精确减半；按波段数组因子；`getNumFactors/getScaleFactors/getOffsets`；`getBounds2D/getPoint2D/createCompatibleDestImage`。
