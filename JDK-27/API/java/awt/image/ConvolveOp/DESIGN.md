# java.awt.image.ConvolveOp 设计案

## 1. API 功能分组
- 常量：`EDGE_ZERO_FILL`、`EDGE_NO_OP`。
- 构造：`ConvolveOp(Kernel,int,RenderingHints)`、`ConvolveOp(Kernel)`。
- 操作：`filter(BufferedImage,BufferedImage)`、`filter(Raster,WritableRaster)`。
- 查询：`getEdgeCondition/getKernel/getBounds2D/getPoint2D/getRenderingHints/createCompatibleDestImage/createCompatibleDestRaster`。

## 2. Java 用户用法
- 卷积/模糊/锐化：`new ConvolveOp(kernel, EDGE_NO_OP, null).filter(src, null)`。
- 单位核（中心 1.0）恒等；单抽头核实现平移（`dest(x,y)=src(x-ox,y-oy)`）。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/ConvolveOp/` 覆盖卷积结果与边缘策略。
- JDK 内部 `BufferedImage.filter(BufferedImageOp)` 对 ConvolveOp 走 RasterOp 卷积路径。
- 图像处理库（模糊/边缘检测）大量使用 Kernel+ConvolveOp。

## 4. 测试设计（指向 ConvolveOpTest.java）
- 白盒：implements BufferedImageOp/RasterOp、常量与方法存在。
- 黑盒：单位核 EDGE_NO_OP 精确保留像素；单抽头平移核 EDGE_ZERO_FILL 断 `dest(x,y)=src(x-1,y-1)` 与边缘零填充；默认构造边缘策略；`getBounds2D/getPoint2D/createCompatibleDestImage/getKernel/getEdgeCondition`。
