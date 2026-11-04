# java.awt.image.AbstractMultiResolutionImage 设计案

## 1. API 功能分组
- **类型**：abstract，extends Image、implements MultiResolutionImage。
- **基类实现**：`getWidth(ImageObserver)/getHeight(ImageObserver)/getSource()/getGraphics()/getProperty(String,ImageObserver)` 委托给 `getResolutionVariant`。
- **子类需实现**：`getResolutionVariant(double,double)`、`getResolutionVariants()`。

## 2. Java 用户如何使用
继承该类并返回多分辨率变体；`getWidth/getHeight` 自动从最佳变体取值；`getSource()` 用变体生成图像数据。`BaseMultiResolutionImage` 是其开箱实现。

## 3. 开源代码实际用法调研
- `java.awt.image.BaseMultiResolutionImage` 继承 AbstractMultiResolutionImage，`getWidth/getHeight/getSource/getProperty` 委托最佳变体（`src/java.desktop/share/classes/java/awt/image/BaseMultiResolutionImage.java`）。
- `test/jdk/java/awt/image/MultiResolutionImage/` 用例验证变体选择与委托（本用例委托语义参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AbstractMultiResolutionImageTest.testBlackBox()`，匿名子类返回 10x20 变体）：
1. getWidth/getHeight 委托变体尺寸；getResolutionVariants 大小；getSource 非空；getProperty 委托。
白盒（`testWhiteBox()`）：public abstract、extends Image、implements MultiResolutionImage。
