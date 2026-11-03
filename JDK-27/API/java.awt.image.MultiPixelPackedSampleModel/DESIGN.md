# java.awt.image.MultiPixelPackedSampleModel 设计案

## 1. API 功能分组
- **类型**：MultiPixelPackedSampleModel extends SampleModel。
- **构造**：`(int dataType,int w,int h,int numberOfBits)`、`(...,int scanlineStride,int dataBitOffset)`。
- **访问**：`getNumDataElements/getSampleSize/getPixelBitStride/getDataBitOffset/getBitOffset/getScanlineStride/getTransferType`。
- **读写**：`getSample/setSample/getPixel/setPixel/getDataElements/setDataElements`。
- **创建**：`createCompatibleSampleModel/createDataBuffer/createSubsetSampleModel`；`equals/hashCode`。

## 2. Java 用户如何使用
描述每个像素 1 位（二值/位图）或多位的打包栅格，多个像素挤在同一字节；`getSample(x,y,b,db)/setSample` 按位读写。

## 3. 开源代码实际用法调研
- `java.awt.image.MultiPixelPackedSampleModel` 用于 1/2/4 位打包图像（`src/java.desktop/share/classes/java/awt/image/MultiPixelPackedSampleModel.java`）。
- `test/jdk/java/awt/image/` 打包样本模型用例验证位偏移与位读写（本用例 1bpp 位操作参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MultiPixelPackedSampleModelTest.testBlackBox()`，TYPE_BYTE 8x1、1bpp）：
1. numDataElements/sampleSize/bitStride/transferType/scanlineStride 精确。
2. setSample(bit0,bit3)/getSample 按位读写、未设位为 0、getPixel。
3. createCompatibleSampleModel/createSubsetSampleModel/getDataElements。
白盒（`testWhiteBox()`）：public、extends SampleModel。
