# java.awt.color.ICC_ColorSpace 设计案

## 1. API 功能分组
- **构造**：`ICC_ColorSpace(ICC_Profile)`。
- **查询**：`getProfile()`；继承 `getType/getNumComponents/getName/getMinValue/getMaxValue/isCS_sRGB`。
- **转换**：`toRGB/fromRGB/toCIEXYZ/fromCIEXYZ(float[])`（基于 ICC profile 实现）。

## 2. Java 用户如何使用
用 `ICC_Profile` 构造色彩空间：`ICC_ColorSpace cs = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_sRGB));`，然后 `cs.toRGB(device)` 做设备色转 RGB、`cs.toCIEXYZ(device)` 转 PCS，配合 `ColorConvertOp` 做图像色彩空间转换。

## 3. 开源代码实际用法调研
- `java.awt.image.ColorConvertOp` 用源/目标 `ICC_ColorSpace` 做图像色彩转换（JDK 源码）。
- `test/jdk/java/awt/color/ICC_ColorSpace.java` 构造 sRGB/gray 的 ICC_ColorSpace 并验证 toRGB/fromRGB 数值（本用例参考其 round-trip 主题）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ICC_ColorSpaceTest.testBlackBox()`）：
1. getProfile 返回原 profile；type/numComponents/isCS_sRGB/name/min-max。
2. sRGB toRGB/fromRGB 与 toCIEXYZ/fromCIEXYZ 两组 round-trip（容差因色彩转换量化）。
3. gray ICC_ColorSpace type=GRAY、1 分量、toRGB 输出 3 分量。
白盒（`testWhiteBox()`）：public class、extends ColorSpace、ICC_Profile 构造、getProfile 与四个转换方法签名。
