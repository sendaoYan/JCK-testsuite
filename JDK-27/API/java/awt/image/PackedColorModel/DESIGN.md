# java.awt.image.PackedColorModel 设计案

## 1. API 功能分组
- **类型**：PackedColorModel（abstract）extends ColorModel。
- **构造**：单像素掩码数组 / 各波段位数字段两种。
- **查询**：`getMask(int)/getMasks()/getPixelSize`。
- **创建/判断**：`createCompatibleSampleModel/isCompatibleSampleModel/getAlphaRaster`；`equals/hashCode`。

## 2. Java 用户如何使用
描述像素打包在单个 int/long 中的颜色模型（如 ARGB 各 8 位）。`getMasks()` 得各波段掩码；`getMask(i)` 得第 i 波段掩码；`DirectColorModel` 是其具体子类。

## 3. 开源代码实际用法调研
- `java.awt.image.PackedColorModel` 是打包颜色模型抽象，`DirectColorModel` 实现（`src/java.desktop/share/classes/java/awt/image/`）。
- `test/jdk/java/awt/image/` 颜色模型用例验证掩码与兼容样本模型（本用例 ARGB 掩码参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`PackedColorModelTest.testBlackBox()`，匿名 32 位 ARGB）：
1. getMask/getMasks（alpha 在前 + RGB）、getPixelSize==32。
2. createCompatibleSampleModel/isCompatibleSampleModel；getAlphaRaster 非空；equals/hashCode。
白盒（`testWhiteBox()`）：public abstract、extends ColorModel。
