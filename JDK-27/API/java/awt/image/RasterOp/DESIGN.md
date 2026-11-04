# java.awt.image.RasterOp 设计案

## 1. API 功能分组
- **类型**：interface。
- **方法**：`filter(Raster,WritableRaster)`、`getBounds2D(Raster)`、`createCompatibleDestRaster(Raster)`、`getPoint2D(Point2D,Point2D)`、`getRenderingHints()`。

## 2. Java 用户如何使用
对 Raster 做像素运算的接口；`BandCombineOp/LookupOp/RescaleOp` 皆实现之。`op.filter(src, null)` 创建新目标栅格返回。

## 3. 开源代码实际用法调研
- `java.awt.image.RasterOp` 是栅格操作接口（`src/java.desktop/share/classes/java/awt/image/RasterOp.java`）。
- `test/jdk/java/awt/image/` RasterOp 实现用例验证 filter/边界（本用例接口契约参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RasterOpTest.testBlackBox()`，匿名实现）：
1. getBounds2D/createCompatibleDestRaster/getPoint2D/filter 调用契约。
白盒（`testWhiteBox()`）：interface、5 个方法签名。
