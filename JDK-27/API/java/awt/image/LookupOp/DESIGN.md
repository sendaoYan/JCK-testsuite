# java.awt.image.LookupOp 设计案

## 1. API 功能分组
- 构造：`LookupOp(LookupTable,RenderingHints)`。
- 查询：`getTable/getBounds2D/getPoint2D/getRenderingHints/createCompatibleDestImage/createCompatibleDestRaster`。
- 操作：`filter(BufferedImage,BufferedImage)`、`filter(Raster,WritableRaster)`。

## 2. Java 用户用法
- 查表映射：`new LookupOp(new ByteLookupTable(0, tables), null).filter(src,null)`；每个样本经表映射。
- 阈值/伽马/反色等映射用 `ByteLookupTable/ShortLookupTable` 表达。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/LookupOp/` 覆盖查表结果。
- JDK 内部 `RescaleOp` 对 byte/short 数据会构造 `ByteLookupTable/ShortLookupTable` 优化为 LookupOp 语义。
- 图像库用 LookupOp 做 LUT 色彩变换。

## 4. 测试设计（指向 LookupOpTest.java）
- 白盒：implements BufferedImageOp/RasterOp、getTable/filter 存在。
- 黑盒：3 波段 byte 表（`min(i,127)`）映射 `(200,100,50)->(127,100,50)`；亚 128 值恒等；`getTable/getBounds2D/getPoint2D/createCompatibleDestImage/getRenderingHints`。
