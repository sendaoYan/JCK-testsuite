# java.awt.image.WritableRaster 设计案

## 1. API 功能分组
- 派生：`getWritableParent/createWritableTranslatedChild/createWritableChild`。
- 写入像素：`setPixel/setPixels/setSample/setSamples`（int/float/double 重载）。
- 写入块：`setDataElements`（Object 与 Raster 两种）、`setRect`（两种重载）。

## 2. Java 用户用法
- 由 `Raster.createInterleavedRaster(...)` 得到；`setPixel(x,y,int[]{r,g,b})`、`setSample(x,y,b,v)`。
- `setRect(dx,dy,src)` 复制区域；`createWritableChild(...)` 返回与父共享数据的可写子栅格。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `BufferedImage.getRaster()` 返回 `WritableRaster`，像素写入都走 `setSample/setPixel`。
- `test/jdk/java/awt/image/GetSamplesTest.java`/`GetDataElementsTest.java` 对 WritableRaster 读写采样。
- 图像库用 `setPixels/setRect` 批量写入像素块。

## 4. 测试设计（指向 WritableRasterTest.java）
- 白盒：extends Raster、写方法与派生方法存在。
- 黑盒：`setPixel/getPixel`、`setSample/getSample`、`setPixels/setSamples`、`setDataElements(byte[])` 往返；`setRect` 复制；`getWritableParent`；`createWritableTranslatedChild/createWritableChild` 共享数据写回父栅格。
