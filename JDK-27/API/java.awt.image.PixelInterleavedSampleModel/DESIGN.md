# java.awt.image.PixelInterleavedSampleModel 设计案

## 1. API 功能分组
- **类型**：PixelInterleavedSampleModel extends ComponentSampleModel。
- **构造**：`(int dataType,int w,int h,int pixelStride,int scanlineStride,int[] bandOffsets)`。
- **创建**：`createCompatibleSampleModel/createSubsetSampleModel`；`hashCode/equals`；继承 ComponentSampleModel 的读写。

## 2. Java 用户如何使用
描述逐像素交错的栅格布局（如 RGBRGBRGB 或 BGRBGRBGR 连续存储）；bandOffsets 控制波段顺序。

## 3. 开源代码实际用法调研
- `java.awt.image.PixelInterleavedSampleModel` 是 ComponentSampleModel 的标准子类（`src/java.desktop/share/classes/java/awt/image/PixelInterleavedSampleModel.java`）。
- `test/jdk/java/awt/image/` 交错样本模型用例验证波段顺序与读写（本用例 BGR 布局参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`PixelInterleavedSampleModelTest.testBlackBox()`，TYPE_BYTE 4x4、BGR 偏移 2/1/0）：
1. stride/bandOffsets(BGR)/numDataElements/sampleSize 精确。
2. createDataBuffer 大小 48；get/setSample、get/setPixel 往返。
3. createCompatibleSampleModel 类型/createSubsetSampleModel/equals/hashCode。
白盒（`testWhiteBox()`）：public、extends ComponentSampleModel。
