# java.awt.Image 黑盒行为测试设计

## 1. API 功能
- 抽象类：`getWidth/getHeight(ImageObserver)`（未知返回 -1）、`getSource()`、`getGraphics()`、`getProperty(name,observer)`。
- 缩放：`getScaledInstance(w,h,hints)`；hints 常量 `SCALE_DEFAULT=1/FAST=2/SMOOTH=4/REPLICATE=8/AREA_AVERAGING=16`。
- 工具：`flush()`、`getCapabilities(gc)`、`setAccelerationPriority/getAccelerationPriority`。
- 常量：`UndefinedProperty`。

## 2. Java 用户如何使用
所有图像（BufferedImage/ToolkitImage/VolatileImage）的公共超类；缩放与能力查询：
```java
Image img = source.getScaledInstance(200, 100, Image.SCALE_SMOOTH);
img.setAccelerationPriority(1.0f);
int w = img.getWidth(null); // 未知时为 -1
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Image.java`：`accelerationPriority` 默认 0.5f；`setAccelerationPriority` 对 <0 或 >1 抛 `IllegalArgumentException`；`getScaledInstance` 宽或高为 0 抛 `IllegalArgumentException`；未知属性返回 `UndefinedProperty`。
- `src/java.desktop/share/classes/java/awt/image/BufferedImage.java`、`VolatileImage.java` 等子类实现抽象方法。
- `test/jdk/java/awt/image/` 大量使用 `getScaledInstance`/`getWidth(null)`。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 缩放常量精确值、`UndefinedProperty` 非空。
- `BufferedImage` 尺寸回读；加速优先级默认 0.5 + 设置/读取 + 越界 IAE。
- `getCapabilities(null)` 非空、`getSource` 非空、未知属性==`UndefinedProperty`。
- `getScaledInstance` 尺寸、零宽 IAE；`flush` 可调用。
- 匿名「未加载」子类 `getWidth/getHeight(null)==-1` 契约。
headless-safe。
