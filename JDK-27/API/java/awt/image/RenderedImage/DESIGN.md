# java.awt.image.RenderedImage 设计案

## 1. API 功能分组
- **类型**：interface。
- **元数据**：`getColorModel/getSampleModel/getWidth/getHeight/getMinX/getMinY/getSources/getProperty/getPropertyNames`。
- **分块**：`getNumXTiles/getNumYTiles/getMinTileX/getMinTileY/getTileWidth/getTileHeight/getTileGridXOffset/getTileGridYOffset/getTile`。
- **数据**：`getData()/getData(Rectangle)/copyData(WritableRaster)`。

## 2. Java 用户如何使用
描述可渲染图像（含分块栅格）的只读视图；`BufferedImage` 实现该接口。`getData(rect)` 取子区域、`copyData` 复制到目标栅格。

## 3. 开源代码实际用法调研
- `java.awt.image.RenderedImage` 是渲染图像核心接口，`BufferedImage` 实现（`src/java.desktop/share/classes/java/awt/image/`）。
- `test/jdk/java/awt/image/` 渲染图像用例基于 BufferedImage 验证分块/数据（本用例 BufferedImage 委托参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RenderedImageTest.testBlackBox()`，BufferedImage 6x4）：
1. 尺寸/原点/颜色模型/样本模型/来源空。
2. 分块计数与 tile 尺寸/getTile；getData/getData(Rectangle)/copyData。
白盒（`testWhiteBox()`）：interface、关键方法存在。
