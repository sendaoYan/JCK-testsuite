# java.awt.color.ColorSpace 设计案

## 1. API 功能分组
- **常量**：颜色空间类型 `TYPE_XYZ=0/TYPE_RGB=5/TYPE_GRAY=6/TYPE_HSV=7/TYPE_CMYK=9` 等；内置空间 `CS_sRGB=1000/CS_CIEXYZ=1001/CS_PYCC=1002/CS_GRAY=1003/CS_LINEAR_RGB=1004`。
- **工厂**：`getInstance(int colorspace)`。
- **转换**：抽象 `toRGB/fromRGB/toCIEXYZ/fromCIEXYZ(float[])`。
- **查询**：`getType()`、`getNumComponents()`、`getName(int)`、`getMinValue(int)`/`getMaxValue(int)`、`isCS_sRGB()`。

## 2. Java 用户如何使用
`ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);` 拿到内置 sRGB，`cs.toRGB(deviceComponents)` 转 RGB，`cs.fromRGB(rgb)` 逆变换；`cs.getNumComponents()` 知分量数；自定义色彩空间需继承并实现四个转换方法。

## 3. 开源代码实际用法调研
- `java.awt.image.ColorModel` 及其子类持有 `ColorSpace` 字段，`getRed/getGreen/getBlue` 内部调 `colorSpace.toRGB(...)`（JDK 源码）。
- `java.awt.color.ICC_ColorSpace` 是 ColorSpace 的 ICC 实现子类（`src/java.desktop/share/classes/java/awt/color/ICC_ColorSpace.java`）。
- `test/jdk/java/awt/color/ICC_ColorSpace.java` 用 toRGB/fromRGB 对拍验证 ICC 色彩空间转换（本用例 round-trip 参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ColorSpaceTest.testBlackBox()`）：
1. TYPE_* 常量精确值；getInstance(CS_sRGB/CS_GRAY) 的 type/numComponents/isCS_sRGB/name/min-max。
2. sRGB toRGB 白色保持；gray toRGB 返回 3 分量。
3. sRGB toRGB/fromRGB round-trip；自定义匿名子类 identity round-trip 与 type/components。
4. 全量 TYPE_*/CS_* 常量精确值（Lab=1 Luv=2 YCbCr=3 Yxy=4 HLS=8 CMY=11 2CLR=12 3CLR=13 FCLR=25；CS_sRGB=1000 CIEXYZ=1001 PYCC=1002 GRAY=1003 LINEAR=1004）。
5. getInstance 对 CS_sRGB/CS_GRAY 幂等（== 同一缓存实例）。
6. CS_CIEXYZ type=XYZ/3 分量、CS_PYCC type=3CLR/3 分量、CS_LINEAR_RGB type=RGB/3 分量且 !isCS_sRGB。
7. sRGB 分量名 Red/Green/Blue；getInstance(0) 与 getName(越界) 均抛 IllegalArgumentException。
8. sRGB toCIEXYZ/fromCIEXYZ round-trip（容差 1e-4）；gray toCIEXYZ(1.0) ≈ D50 白点 (0.9642,1.0,0.8249)；gray toRGB/fromRGB round-trip。
白盒（`testWhiteBox()`）：abstract public class、implements Serializable、TYPE_*/CS_* 常量、getInstance 与四个抽象转换方法签名。
