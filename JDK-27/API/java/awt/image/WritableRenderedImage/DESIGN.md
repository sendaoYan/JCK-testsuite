# java.awt.image.WritableRenderedImage 设计案

## 1. API 功能分组
- **类型**：interface extends RenderedImage。
- **tile 写管理**：`getWritableTile/releaseWritableTile/isTileWritable/getWritableTileIndices/hasTileWriters`。
- **观察者**：`addTileObserver/removeTileObserver`。
- **数据**：`setData(Raster)`。

## 2. Java 用户如何使用
可写渲染图像的 tile 管理协议：`getWritableTile(tx,ty)` 取可写 tile、`releaseWritableTile` 释放；`setData` 整体写入栅格；`addTileObserver` 监听 tile 事件。

## 3. 开源代码实际用法调研
- `java.awt.image.WritableRenderedImage` 由 `BufferedImage` 实现（`src/java.desktop/share/classes/java/awt/image/BufferedImage.java`）。
- `test/jdk/java/awt/image/BufferedImage/` 用例验证 tile 写协议与 setData（本用例 BufferedImage 委托参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`WritableRenderedImageTest.testBlackBox()`，BufferedImage 4x4）：
1. getWritableTile/isTileWritable/hasTileWriters/getWritableTileIndices/releaseWritableTile 状态流。
2. setData 写入并读取校验。
白盒（`testWhiteBox()`）：interface、extends RenderedImage、8 个方法存在。
