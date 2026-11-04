# java.awt.color.ICC_ProfileGray 设计案

## 1. API 功能分组
- **类型**：`ICC_ProfileGray extends ICC_Profile`（final），仅由 `ICC_Profile.getInstance(...)` 构造。
- **查询**：`getMediaWhitePoint()`（3 分量 XYZ 白点）、`getGamma()`（单 gamma TRC）、`getTRC()`（表 TRC）。

## 2. Java 用户如何使用
`ICC_ProfileGray g = (ICC_ProfileGray) ICC_Profile.getInstance(ColorSpace.CS_GRAY);` 拿灰度 profile；`g.getGamma()` 当 TRC 为单 gamma 时取 gamma 值；`g.getTRC()` 当 TRC 为表时取 16 位查找表；两者互斥，另一方抛 ProfileDataException。

## 3. 开源代码实际用法调研
- `ICC_ProfileGray` 在 `ICC_Profile.getInstance(int)` 中按 `CS_GRAY` 分支返回（`src/java.desktop/share/classes/java/awt/color/ICC_Profile.java` 851 行附近）。
- JDK 内置灰 profile 的 TRC 为线性 gamma 1.0，因此 `getGamma()==1.0` 而 `getTRC()` 抛 ProfileDataException（源码 `getGamma`/`getTRC` 注释明确说明该互斥契约）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ICC_ProfileGrayTest.testBlackBox()`）：
1. getInstance(CS_GRAY) 返回 ICC_ProfileGray 实例。
2. getMediaWhitePoint 3 分量且为正。
3. getGamma()==1.0（线性 TRC）；getTRC() 抛 ProfileDataException（互斥契约）。
白盒（`testWhiteBox()`）：public final、extends ICC_Profile、getMediaWhitePoint/getGamma/getTRC 签名。
