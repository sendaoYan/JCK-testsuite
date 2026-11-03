# java.awt.image.DirectColorModel 设计案

## 1. API 功能分组
- 构造：`DirectColorModel(int bits, int rmask, int gmask, int bmask[, int amask])`、带 `ColorSpace` 与预乘标志的完整构造。
- 掩码查询：`getRedMask/getGreenMask/getBlueMask/getAlphaMask`（继承 `getMasks/getMask`）。
- 像素/组件：`getRed/getGreen/getBlue/getAlpha/getRGB`（int 与 Object）、`getDataElements/getDataElement/getComponents`。
- 兼容性：`createCompatibleWritableRaster/isCompatibleRaster/coerceData`。

## 2. Java 用户用法
- 标准 ARGB：`new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF, 0xFF000000)`；`getRed(px)` 从掩码提取并归一化到 0–255。
- 5-6-5 真彩：`new DirectColorModel(16, 0xF800, 0x07E0, 0x001F)`；分量自动放大到满量程。
- 无 alpha 3 掩码模型强制 alpha=255。

## 3. 开源实际用法调研（来源：JDK 源码/自测）
- `test/jdk/java/awt/image/PackedColorModel/` 覆盖打包颜色模型（DirectColorModel 的父类）。
- JDK 内部屏幕缓冲区与 `ColorModel.getRGBdefault()` 使用 DirectColorModel。
- 图像库用掩码构造 DirectColorModel 表示设备像素格式。

## 4. 测试设计（指向 DirectColorModelTest.java）
- 白盒：extends PackedColorModel、掩码/组件方法存在。
- 黑盒：32 位 4 掩码模型掩码值、像素尺寸、透明度；`getRed/Green/Blue/Alpha/getRGB` 精确（含极端值）；3 掩码无 alpha 强制 opaque；5-6-5 分量放大到 255；`getComponents/getDataElement` 往返；兼容 raster。
