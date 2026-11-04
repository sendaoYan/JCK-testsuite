# java.awt.image.ReplicateScaleFilter 设计案

## 1. API 功能分组
- 构造：`ReplicateScaleFilter(int width, int height)`。
- 重写：`setProperties/setDimensions/setPixels(...byte[]...)/setPixels(...int[]...)`。

## 2. Java 用户用法
- 快速缩放（复制像素）：`new FilteredImageSource(src, new ReplicateScaleFilter(w,h))` 再 `PixelGrabber`；上采样复制行列、下采样丢行丢列。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `Image.getScaledInstance` 的 `SCALE_REPLICATE` 使用 `ReplicateScaleFilter`。
- `AreaAveragingScaleFilter` 继承自该类。
- 图像库用复制缩放做快速整数倍放大。

## 4. 测试设计（指向 ReplicateScaleFilterTest.java）
- 白盒：extends ImageFilter、setDimensions/setPixels 存在。
- 黑盒：2x2→4x4 上采样每个源像素复制成 2x2 块（逐像素断言）；1:1 精确保留。
