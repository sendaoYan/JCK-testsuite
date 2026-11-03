# java.awt.image.MultiResolutionImage 设计案

## 1. API 功能分组
- **类型**：interface。
- **方法**：`getResolutionVariant(double destWidth, double destHeight)`、`getResolutionVariants()`。

## 2. Java 用户如何使用
图像实现多分辨率支持，按目标尺寸返回合适变体；`Graphics2D` 渲染时自动取变体适配屏幕缩放。

## 3. 开源代码实际用法调研
- `java.awt.image.MultiResolutionImage` 接口，`AbstractMultiResolutionImage/BaseMultiResolutionImage` 实现（`src/java.desktop/share/classes/java/awt/image/`）。
- `test/jdk/java/awt/image/MultiResolutionImage/` 用例验证变体选择（本用例接口契约参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MultiResolutionImageTest.testBlackBox()`，匿名实现）：
1. getResolutionVariant 返回变体；getResolutionVariants 大小。
白盒（`testWhiteBox()`）：interface、两个方法签名。
