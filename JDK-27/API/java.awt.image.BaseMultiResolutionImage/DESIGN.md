# java.awt.image.BaseMultiResolutionImage 设计案

## 1. API 功能分组
- **类型**：BaseMultiResolutionImage extends AbstractMultiResolutionImage。
- **构造**：`BaseMultiResolutionImage(Image...)`、`BaseMultiResolutionImage(int baseWidth, Image...)`。
- **查询**：`getResolutionVariant(double,double)`（选宽高都 >= 请求尺寸的最小变体）、`getResolutionVariants()`。

## 2. Java 用户如何使用
`new BaseMultiResolutionImage(img1x, img2x, img3x)` 提供多分辨率变体；`getResolutionVariant(20, 20)` 自动挑最合适的变体供渲染。

## 3. 开源代码实际用法调研
- `java.awt.image.BaseMultiResolutionImage` 是 JDK 标准多分辨率图像实现（`src/java.desktop/share/classes/java/awt/image/BaseMultiResolutionImage.java`）。
- `test/jdk/java/awt/image/MultiResolutionImage/` 用例验证变体选择策略（本用例 "最小 ≥ 请求尺寸" 规则参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`BaseMultiResolutionImageTest.testBlackBox()`，10/20/30 变体）：
1. getResolutionVariants 顺序；15->20、40->30、5->10 变体选择。
2. getWidth/getHeight 委托默认变体；base-width 构造去基图、宽度 30。
白盒（`testWhiteBox()`）：public、extends AbstractMultiResolutionImage。
