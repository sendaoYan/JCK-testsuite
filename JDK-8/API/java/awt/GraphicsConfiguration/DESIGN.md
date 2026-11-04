# java.awt.GraphicsConfiguration 黑盒行为测试设计（JDK-8）

## 1. API 功能
抽象类，描述 GraphicsDevice 的渲染能力：getDevice、createCompatibleImage(w,h)/(w,h,transparency)、
createCompatibleVolatileImage(w,h)/(w,h,transparency)、getColorModel()/getColorModel(transparency)、
getDefaultTransform/getNormalizingTransform/getBounds、getBufferCapabilities/getImageCapabilities、
isTranslucencyCapable。

## 2. Java 用户如何使用
`GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
    .getDefaultScreenDevice().getDefaultConfiguration();`
`BufferedImage img = gc.createCompatibleImage(8, 8); img.setRGB(0,0,0xFF000000);`

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/GraphicsConfiguration.java`
（createCompatibleImage = colorModel.createCompatibleWritableRaster 包 BufferedImage；默认
isTranslucencyCapable false；getBufferCapabilities 由 imageCapabilities 构造）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集）→ `testStructure`。
- getDevice 同一性、bounds 正尺寸、colorModel(OPAQUE) 非 null、双 transform 非 null、
  缓冲/图像能力非 null、像素黑白精确往返、TRANSLUCENT 条件断言 → `testBlackBox`。
- **设备全部配置不变量限定前 10 个（易错：X11 暴露约 200 个 visual，无界循环断言膨胀）**。
- volatile 重载尺寸精确 + 反例：负宽/零高等 → IllegalArgumentException
  （createCompatibleImage 与 createCompatibleVolatileImage 均校验）。
- isTranslucencyCapable 两次调用稳定。
- JDK-8 侧 92 断言通过（DISPLAY=:77，@key headful；不锁分辨率/色彩模型类型）。
