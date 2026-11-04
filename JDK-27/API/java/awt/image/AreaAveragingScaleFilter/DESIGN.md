# java.awt.image.AreaAveragingScaleFilter 设计案

## 1. API 功能分组
- 构造：`AreaAveragingScaleFilter(int width, int height)`。
- 重写：`setHints(int)`、`setPixels(...byte[]...)`、`setPixels(...int[]...)`。
- 继承：`setDimensions/setProperties`（来自 ReplicateScaleFilter）。

## 2. Java 用户用法
- 高质量缩放：`new FilteredImageSource(src, new AreaAveragingScaleFilter(w,h))` 再经 `PixelGrabber` 取结果；下采样时对覆盖区域求平均。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `Image` 缩放实现基于 `AreaAveragingScaleFilter`（`java.awt.Image.getScaledInstance` 的 `SCALE_AREA_AVERAGING`）。
- `test/jdk/java/awt/image/FilteredImageSourceTest.java` 间接触发缩放过滤器。
- 图像库用面积平均法生成缩略图。

## 4. 测试设计（指向 AreaAveragingScaleFilterTest.java）
- 白盒：extends ReplicateScaleFilter、setPixels 存在。
- 黑盒：4x4→2x2 下采样对每个恒定 2x2 块求平均得到精确值；1:1 缩放精确保留。
