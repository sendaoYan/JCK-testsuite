# java.awt.image.BandedSampleModel 设计案

## 1. API 功能分组
- 构造：`BandedSampleModel(int dataType, int w, int h, int numBands)`、带 `bankIndices/bandOffsets` 重载。
- 派生：`createCompatibleSampleModel/createSubsetSampleModel/createDataBuffer`。
- 采样读写：`getSample/setSample/getSampleFloat/getSampleDouble/getPixel/setPixel/getPixels/setPixels/getSamples/setSamples/getDataElements/setDataElements`。

## 2. Java 用户用法
- 波段分离布局（每个波段一个 bank）：`new BandedSampleModel(TYPE_BYTE, w, h, bands)`。
- `sm.getSample(x,y,b,db)`/`setSample(...)` 逐波段读写；`createCompatibleSampleModel(w,h)` 派生。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/BandedSampleModel/` 覆盖波段分离模型。
- JDK 内部 `Raster.createBandedRaster` 生成 `BandedSampleModel` 布局。
- 多光谱遥感图像常用 banded 布局。

## 4. 测试设计（指向 BandedSampleModelTest.java）
- 白盒：final、extends ComponentSampleModel、采样/派生方法存在。
- 黑盒：3 波段 byte 模型几何/波段/数据元素/位宽；`setSample/getSample` 往返；`setPixel/getPixel`；`getDataElements/setDataElements`；`createCompatibleSampleModel/createSubsetSampleModel`；`createDataBuffer` 返回 DataBufferByte 且多 bank offset。
