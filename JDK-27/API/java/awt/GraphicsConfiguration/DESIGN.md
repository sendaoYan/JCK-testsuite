# java.awt.GraphicsConfiguration 黑盒行为测试设计

## 1. API 功能
抽象类，描述 GraphicsDevice 的渲染能力（色彩模型/变换/缓冲能力/兼容图像）。
- 设备：`getDevice()`。
- 兼容图像：`createCompatibleImage(int,int)`、`createCompatibleImage(int,int,int transparency)`、`createCompatibleVolatileImage(...)`。
- 颜色：`getColorModel()`、`getColorModel(int transparency)`。
- 变换/几何：`getDefaultTransform()`、`getNormalizingTransform()`、`getBounds()`。
- 能力：`getBufferCapabilities()`、`getImageCapabilities()`、`isTranslucencyCapable()`。

## 2. Java 用户如何使用
```java
GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice().getDefaultConfiguration();
Rectangle b = gc.getBounds();
ColorModel cm = gc.getColorModel();
BufferedImage img = gc.createCompatibleImage(64, 64);
img.setRGB(0, 0, 0xFF000000);
int px = img.getRGB(0, 0);
```
组合链路：`getDefaultScreenDevice().getDefaultConfiguration()` → `getDevice/getBounds/getColorModel/getDefaultTransform/getBufferCapabilities` 能力枚举 → `createCompatibleImage` → `setRGB/getRGB` 像素往返。

## 3. 开源代码实际用法调研（来源标注）
- JDK 自带测试 `test/jdk/java/awt/Graphics2D/TransformSetGet/TransformSetGet.java`：`getDefaultScreenDevice().getDefaultConfiguration()` + `createCompatibleVolatileImage(200,200)`。
- JDK 源码 `src/java.desktop/share/classes/java/awt/GraphicsConfiguration.java`：`createCompatibleImage(int,int)` = `getColorModel().createCompatibleWritableRaster(w,h)` 包 `BufferedImage`；`getBufferCapabilities()` 用 `getImageCapabilities()` 构造 `DefaultBufferCapabilities`；`isTranslucencyCapable()` 默认 false。
- `src/java.desktop/share/classes/java/awt/Component.java`：`createVolatileImage` 内部经 `getGraphicsConfiguration()` 路由。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，@key headful，Xvfb :98，铁律：不断言具体分辨率/色彩模型类型）：
- `getDevice()` 恒等于来源 `GraphicsDevice`。
- `getBounds()` 非 null 且宽高 >0；`getColorModel()`、`getColorModel(OPAQUE)` 非 null。
- `getDefaultTransform()/getNormalizingTransform()` 非 null；`getBufferCapabilities()` 及其前后缓冲能力非 null；`getImageCapabilities()` 非 null；`isTranslucencyCapable()` 可调用。
- `createCompatibleImage(8,8)` 尺寸正确，黑/白像素 setRGB/getRGB 精确往返。
- `createCompatibleImage(8,8,OPAQUE)` 尺寸正确；负宽/负高/零宽抛 `IllegalArgumentException`。
- 若 `getColorModel(TRANSLUCENT)` 非 null，则 `createCompatibleImage(...,TRANSLUCENT)` 非 null（条件断言）。
断言 41 项，白盒 15 + 黑盒 26。
