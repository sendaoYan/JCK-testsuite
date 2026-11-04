# javax.imageio.ImageWriteParam — DESIGN

## 1. API 功能
`ImageWriteParam` 继承 `IIOParam`，控制编码。四个模式常量 `MODE_DISABLED/MODE_DEFAULT/MODE_EXPLICIT/MODE_COPY_FROM_METADATA`。方法分组：
- 平铺：`canWriteTiles/canOffsetTiles/setTilingMode/getTilingMode/setTiling/unsetTiling/getTileWidth/Height/getTileGridX/YOffset/getPreferredTileSizes`。
- 渐进式：`canWriteProgressive/setProgressiveMode/getProgressiveMode`。
- 压缩：`canWriteCompressed/setCompressionMode/getCompressionMode/getCompressionTypes/setCompressionType/getCompressionType/unsetCompression/getLocalizedCompressionTypeName/isCompressionLossless/setCompressionQuality/getCompressionQuality/getBitRate/getCompressionQualityDescriptions/getCompressionQualityValues`。
- 其它：`getLocale`。

## 2. Java 用户如何使用
`ImageWriter w = ...; ImageWriteParam p = w.getDefaultWriteParam(); if (p.canWriteCompressed()) { p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); p.setCompressionType("..."); p.setCompressionQuality(0.5f); } w.write(null, new IIOImage(img,null,null), p);`。

## 3. 开源代码实际用法调研（来源：test/jdk）
- `test/jdk/javax/imageio/plugins/bmp/CompressionModeTest.java`：设置 BMP 压缩模式与 `MODE_EXPLICIT`。
- `test/jdk/javax/imageio/plugins/shared/CanWriteSequence.java`：验证 `canWriteSequence` 能力查询。
- `src/java.desktop/share/classes/javax/imageio/ImageWriteParam.java`：默认 `canWrite*` 均为 false，`setCompressionMode` 在 `!canWriteCompressed()` 时抛 `UnsupportedOperationException`（源码内）。

## 4. 测试用例设计 + 自动化实现（`ImageWriteParamTest.testBlackBox`）
- 默认参数：locale 存储、canWriteTiles/Compressed/Progressive/OffsetTiles 全 false，压缩 setter 抛 UnsupportedOperationException。
- 自建子类开启全部能力：tiling mode/setTiling/unsetTiling、progressive mode（MODE_EXPLICIT 抛 IAE）、compression 模式/类型/质量/isCompressionLossless/getBitRate/unsetCompression。
- 异常：未知压缩类型、质量越界、非法模式、非 EXPLICIT 下访问压缩参数抛 IllegalStateException。
- 白盒：extends IIOParam + 四个 MODE 常量值 + 全部方法存在。
