# java.awt.image.ColorModel 设计案

## 1. API 功能分组
- 工厂：`getRGBdefault()`。
- 查询：`getPixelSize/getComponentSize/getTransparency/getNumComponents/getNumColorComponents/hasAlpha/isAlphaPremultiplied/getTransferType/getColorSpace`。
- 组件提取/合成：`getRed/getGreen/getBlue/getAlpha/getRGB`（int 与 Object 两种）、`getComponents/getDataElement/getDataElements`、`getNormalizedComponents/getUnnormalizedComponents`。
- 兼容性：`createCompatibleWritableRaster/createCompatibleSampleModel/isCompatibleRaster/isCompatibleSampleModel/getAlphaRaster/coerceData`。

## 2. Java 用户用法
- 用 `ColorModel.getRGBdefault()` 拿默认 32 位 sRGB 模型；`getRed/getGreen/getBlue/getAlpha/getRGB` 拆解/合成像素。
- `cm.createCompatibleWritableRaster(w,h)` 生成兼容 `WritableRaster`。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/ColorModel/`、`ComponentColorModel/`、`IndexColorModel/` 覆盖各子类。
- JDK 内部 `java.awt.image` 渲染与 `java.awt.image.BufferedImage` 大量使用 `getRGBdefault()`。
- 图像读写库（ImageIO）用 `getComponentSize/hasAlpha` 判断像素布局。

## 4. 测试设计（指向 ColorModelTest.java）
- 白盒：abstract、implements Transparency、关键方法存在。
- 黑盒：用 `getRGBdefault()` 断言 32 位/4 组件/含 alpha/非预乘/INT 传输/sRGB；`getRed/Green/Blue/Alpha/getRGB` 精确；`getComponents/getDataElement` 往返；`createCompatible*` 与 `isCompatible*` 一致性。
