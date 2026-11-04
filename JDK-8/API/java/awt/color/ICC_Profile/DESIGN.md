# java.awt.color.ICC_Profile 设计案

## 1. API 功能分组
- **常量**：profile 类别（CLASS_INPUT/DISPLAY/OUTPUT/DEVICELINK/...）、色彩空间签名（icSigRgbData/icSigGrayData/icSigCmykData/...）、tag 签名（icSigHead/icSigAToB0Tag/icSigProfileDescriptionTag/...）、头字段偏移（icHdrSize/icHdrCmmId/...）。
- **工厂**：`getInstance(int colorspace)`、`getInstance(byte[] data)`、`getInstance(String fileName)`、`getInstance(InputStream)`。
- **查询**：`getMajorVersion/getMinorVersion/getProfileClass/getColorSpaceType/getPCSType/getNumComponents`。
- **数据读写**：`getData()`/`getData(int tag)`/`setData(int tag, byte[])`/`write(String)`/`write(OutputStream)`。

## 2. Java 用户如何使用
`ICC_Profile p = ICC_Profile.getInstance(ColorSpace.CS_sRGB);` 拿内置 profile；`p.getData()` 序列化，`ICC_Profile.getInstance(bytes)` 反序列化；`p.getData(ICC_Profile.icSigHead)` 读特定 tag；`p.write(os)` 写出；构造 `new ICC_ColorSpace(p)` 使用。

## 3. 开源代码实际用法调研
- `java.awt.color.ICC_ColorSpace` 的构造器接收 ICC_Profile 并实现色彩转换（JDK 源码）。
- `test/jdk/java/awt/color/ICC_Profile.java` 验证 getInstance(byte[])/getData 序列化 round-trip 与 tag 读写（本用例参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ICC_ProfileTest.testBlackBox()`）：
1. sRGB profile 的 numComponents/colorSpaceType/PCS/profileClass/major/minor。
2. getData 非空、getData(icSigHead) 128 字节；byte[] round-trip 得到等价 profile。
3. write(OutputStream) 与 getData 同长；gray profile 1 分量 GRAY 类型。
4. getInstance(非法 colorspace) 抛 IllegalArgumentException。
5. getInstance(CS_sRGB/CS_GRAY) 幂等（== 同一缓存实例）。
6. CLASS_* 常量精确值（INPUT=0 DISPLAY=1 OUTPUT=2 DEVICELINK=3 COLORSPACECONVERSION=4 ABSTRACT=5 NAMEDCOLOR=6）；gray profile 类为 CLASS_DISPLAY；CS_CIEXYZ profile colorSpaceType/PCSType 均 XYZ。
7. getData(未知 tag 签名) 返回 null（tag 缺失语义）。
8. getInstance(垃圾字节) 抛 IllegalArgumentException；getInstance((String)null) 抛 NullPointerException。
9. byte[] 派生 profile 是独立实例且可写：setData(icSigHead) 读回一致、getMajorVersion 反映原始头字节。内置 profile 共享缓存，setData 直接抛 IllegalArgumentException("Can't modify built-in profile")（JDK-9+ 防护；JDK-8 无此防护且写内置会污染同 VM 后续使用——JDK-8 侧只对派生 profile 做同字节写断言，绝不改内置）。
白盒（`testWhiteBox()`）：public class、implements Serializable、关键常量、四个 getInstance 工厂与 getData/setData/write 签名。
