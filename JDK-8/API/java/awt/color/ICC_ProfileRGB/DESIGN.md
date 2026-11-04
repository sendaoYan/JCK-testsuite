# java.awt.color.ICC_ProfileRGB 设计案

## 1. API 功能分组
- **类型**：`ICC_ProfileRGB extends ICC_Profile`（JDK-8 为非 final 的 public class；JDK-9+ 改为 final），仅由 `ICC_Profile.getInstance(...)` 构造。
- **常量**：`REDCOMPONENT=0/GREENCOMPONENT=1/BLUECOMPONENT=2`。
- **查询**：`getMediaWhitePoint()`（3 分量 XYZ 白点）、`getMatrix()`（3x3 RGB→XYZ 色度矩阵）、`getGamma(int)`（单 gamma TRC）、`getTRC(int)`（表 TRC）。

## 2. Java 用户如何使用
`ICC_ProfileRGB rgb = (ICC_ProfileRGB) ICC_Profile.getInstance(ColorSpace.CS_sRGB);` 拿 sRGB；`rgb.getMediaWhitePoint()` 取白点；`rgb.getMatrix()` 取 RGB→XYZ 矩阵；`rgb.getTRC(ICC_ProfileRGB.REDCOMPONENT)` 取红通道 TRC 表。getGamma/getTRC 互斥：表型 TRC 时 getGamma 抛 ProfileDataException，反之亦然。

## 3. 开源代码实际用法调研
- `ICC_ProfileRGB` 在 `ICC_Profile.getInstance(int)` 按 `CS_sRGB/CS_LINEAR_RGB/CS_PYCC` 分支返回（`src/java.desktop/share/classes/java/awt/color/ICC_Profile.java` 849-855 行附近）。
- sRGB 内置 profile 的 TRC 是 1024 项查找表（非单 gamma），因此 getGamma 抛 ProfileDataException、getTRC 返回 short[]（运行时实测确认，且源码注释说明互斥契约）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ICC_ProfileRGBTest.testBlackBox()`）：
1. 组件常量 0/1/2；getInstance(CS_sRGB) 返回 ICC_ProfileRGB。
2. 白点 ~D65（0.9505,1.0,1.089）；矩阵 3x3。
3. 表型 TRC：getGamma(red) 抛 ProfileDataException，getTRC(red) 返回非空表。
4. getGamma(99) 抛 IllegalArgumentException（非法组件）。
白盒（`testWhiteBox()`）：JDK-8 侧断言 public 非 final（JDK-27 侧断言 public final，版本差异）、extends ICC_Profile、组件常量与 getMediaWhitePoint/getMatrix/getGamma/getTRC 签名。
