# java.awt.image.SampleModel 设计案

## 1. API 功能分组
- 查询：`getWidth/getHeight/getNumBands/getDataType/getTransferType/getNumDataElements/getSampleSize`（数组与单波段）。
- 采样读写：`getPixel/setPixel/getPixels/setPixels/getSample/setSample/getSamples/setSamples`（int/float/double，均带 DataBuffer 参数）。
- 数据元素：`getDataElements/setDataElements`。
- 派生：`createCompatibleSampleModel/createSubsetSampleModel/createDataBuffer`。

## 2. Java 用户用法
- 抽象类，由 `Raster.getSampleModel()` 或具体子类实例化；`sm.getSample(x,y,b,db)` 从 DataBuffer 采样。
- `sm.createCompatibleSampleModel(w,h)` 生成同布局新模型；`createDataBuffer()` 生成匹配缓冲。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/SampleModelConstructorTest.java`、`GetDataElementsTest.java`、`GetSamplesTest.java` 覆盖采样模型。
- JDK 内部 `Raster` 全部通过 `SampleModel` 计算样本偏移与读取。
- 图像库用 `createCompatibleSampleModel/createDataBuffer` 复制图像布局。

## 4. 测试设计（指向 SampleModelTest.java）
- 白盒：abstract、采样/派生方法存在。
- 黑盒：用 interleaved 3 波段模型断言几何/波段/类型/样本位宽/数据元素数；`setSample/getSample/setPixel/getPixel` 往返；`createDataBuffer`、`createCompatibleSampleModel`、`createSubsetSampleModel`；`getDataElements/setDataElements` 往返。
