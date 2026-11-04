# java.awt.image.ComponentSampleModel 设计案

## 1. API 功能分组
- **类型**：ComponentSampleModel extends SampleModel。
- **构造**：`(int dataType,int w,int h,int pixelStride,int scanlineStride,int[] bandOffsets)` 及带 bankIndices 重载。
- **访问**：`getOffset(x,y)/(x,y,b)`、`getSampleSize/getBankIndices/getBandOffsets/getScanlineStride/getPixelStride/getNumDataElements`。
- **读写**：`getSample*/setSample*`、`getPixel/setPixel/getPixels/setPixels`、`getDataElements/setDataElements`。
- **创建**：`createCompatibleSampleModel/createSubsetSampleModel/createDataBuffer`；`equals/hashCode`。

## 2. Java 用户如何使用
描述多波段逐分量存储的栅格布局（RGB 三波段连续存放）。`createDataBuffer` 得到存储，`getSample(x,y,b,db)/setSample` 逐波段读写。

## 3. 开源代码实际用法调研
- `java.awt.image.ComponentSampleModel` 是标准 SampleModel 实现（`src/java.desktop/share/classes/java/awt/image/ComponentSampleModel.java`），`PixelInterleavedSampleModel/BandedSampleModel` 皆其子类。
- `test/jdk/java/awt/image/` 样本模型用例验证 offset/stride/sample 读写（本用例布局计算参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ComponentSampleModelTest.testBlackBox()`，TYPE_BYTE 4x4、pixelStride3/scanlineStride12/偏移 0,1,2）：
1. numDataElements/sampleSize/bankIndices/bandOffsets/stride/offset 精确值。
2. createDataBuffer 大小 48；get/setSample、get/setPixel 往返。
3. createCompatibleSampleModel/createSubsetSampleModel/getDataElements/equals/hashCode。
白盒（`testWhiteBox()`）：public、extends SampleModel。
