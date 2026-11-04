# java.awt.image.SinglePixelPackedSampleModel 设计案

## 1. API 功能分组
- 构造：`SinglePixelPackedSampleModel(int dataType, int w, int h, int[] bitMasks)`、带 `scanlineStride` 重载。
- 查询：`getNumDataElements/getSampleSize/getSampleSize(int)/getBitMasks/getBitOffsets/getOffset/getScanlineStride`。
- 采样读写：`getSample/setSample/getPixel/setPixel/getSamples/setSamples/getDataElements/setDataElements`。
- 派生：`createCompatibleSampleModel/createSubsetSampleModel/createDataBuffer`。

## 2. Java 用户用法
- 打包像素（如 RGB565/ARGB8888）：`new SinglePixelPackedSampleModel(TYPE_INT, w, h, new int[]{0xFF0000,0xFF00,0xFF})`。
- `sm.getSample(x,y,b,db)` 提取某分量；`getBitMasks()/getBitOffsets()` 得到掩码与位移。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- JDK 内部 `DirectColorModel.createCompatibleSampleModel` 生成 `SinglePixelPackedSampleModel`。
- `test/jdk/java/awt/image/GetSamplesTest.java` 对打包样本模型采样。
- 图像库用打包模型表示单 int 像素的多波段布局。

## 4. 测试设计（指向 SinglePixelPackedSampleModelTest.java）
- 白盒：extends SampleModel、掩码/偏移方法存在。
- 黑盒：3 波段 32 位模型，几何/数据元素/传输类型/扫描线步长/偏移；`getBitMasks/getBitOffsets/getSampleSize`；`setSample/getSample` 合成/拆解精确 int；`setPixel/getPixel`；`createDataBuffer/createCompatibleSampleModel/createSubsetSampleModel`；`getDataElements/setDataElements` 往返；equals/hashCode。
