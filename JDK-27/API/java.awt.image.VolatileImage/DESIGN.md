# java.awt.image.VolatileImage 设计案

## 1. API 功能分组
- **类型**：VolatileImage（abstract）extends Image、implements Transparency。
- **状态常量**：`IMAGE_OK=0/IMAGE_RESTORED=1/IMAGE_INCOMPATIBLE=2`。
- **抽象方法**：`getSnapshot/getWidth/getHeight/createGraphics/validate/contentsLost/getCapabilities`。
- **具体方法**：`getSource/getGraphics/getTransparency`（继承/实现）。

## 2. Java 用户如何使用
硬件加速、可丢失的离屏图像：`validate(gc)` 返回 IMAGE_OK/IMAGE_INCOMPATIBLE 判断可用性，`createGraphics()` 绘图，`getSnapshot()` 取软件副本。

## 3. 开源代码实际用法调研
- `java.awt.image.VolatileImage` 是易失图像抽象（`src/java.desktop/share/classes/java/awt/image/VolatileImage.java`）。
- `test/jdk/java/awt/image/VolatileImage/` 用例验证 validate/contentsLost 状态（本用例状态常量参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`VolatileImageTest.testBlackBox()`，匿名 4x5）：
1. 状态常量精确值 0/1/2。
2. getWidth/getHeight/getSnapshot/contentsLost/validate/getCapabilities/getTransparency(OPAQUE)。
白盒（`testWhiteBox()`）：public abstract、extends Image、implements Transparency、状态常量互异。
