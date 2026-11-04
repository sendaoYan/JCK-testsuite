# java.awt.image.Raster 设计案

## 1. API 功能分组
- 工厂：`createInterleavedRaster/createBandedRaster/createPackedRaster`（多种重载）、`createRaster/createWritableRaster`。
- 几何：`getBounds/getMinX/getMinY/getWidth/getHeight`。
- 采样：`getPixel/getPixels/getSample/getSampleFloat/getSampleDouble/getSamples/getDataElements`。
- 元数据/派生：`getNumBands/getNumDataElements/getTransferType/getDataBuffer/getSampleModel/getParent`、`createCompatibleWritableRaster`（多重重载）、`createTranslatedChild/createChild`。

## 2. Java 用户用法
- `Raster.createInterleavedRaster(TYPE_BYTE,w,h,bands,null)` 得到 `WritableRaster`；`getSample(x,y,b)` 采样。
- `createTranslatedChild(dx,dy)` 得到带偏移的子栅格；`createChild(...)` 截取子区域。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/Raster/`、`RasterCreationTest.java`、`GetSamplesTest.java` 覆盖工厂与采样。
- JDK 内部 `BufferedImage.getRaster()` 返回的 `WritableRaster` 是 Raster 子类。
- 图像库通过 `Raster` 抽象统一访问不同布局（interleaved/banded/packed）像素。

## 4. 测试设计（指向 RasterTest.java）
- 白盒：静态工厂、采样、派生方法存在。
- 黑盒：interleaved 3 波段栅格几何/波段/传输类型；`setSample/getSample/getPixel/getSamples`；`createCompatibleWritableRaster` 三种重载；`createTranslatedChild/createChild` 偏移与父子关系；banded/packed 工厂波段数与数据元素数。
