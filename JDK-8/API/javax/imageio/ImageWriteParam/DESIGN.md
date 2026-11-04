# javax.imageio.ImageWriteParam — DESIGN

## 1. API 功能
`ImageWriteParam` 扩展 `IIOParam`，增加写入侧三种可选特性（平铺/渐进/压缩）的状态机，每种特性以 4 个模式常量驱动：`MODE_DISABLED=0`、`MODE_DEFAULT=1`、`MODE_EXPLICIT=2`、`MODE_COPY_FROM_METADATA=3`。能力由 protected 布尔字段声明（`canWriteTiles/canOffsetTiles/canWriteProgressive/canWriteCompressed`），子类构造器内设置。
- 平铺：`setTilingMode`（默认 COPY_FROM_METADATA）；仅 EXPLICIT 下 `setTiling(w,h,xOff,yOff)`（w/h<=0 IAE；偏移需 canOffsetTiles 否则 UOE）、`getTileWidth` 等 4 getter（未 setTiling 时 ISE）、`unsetTiling`；`getPreferredTileSizes` 返回 clone。
- 渐进：`setProgressiveMode` 仅接受 DISABLED/DEFAULT/COPY_FROM_METADATA（EXPLICIT 抛 IAE，插件不透出）。
- 压缩：`setCompressionMode`；EXPLICIT 下 `getCompressionTypes`（clone）、`setCompressionType`（不在表内 IAE、null 清空、无表时 UOE）、`setCompressionQuality`（[0,1] 边界含端点）、`getBitRate`（质量参数域校验）、`unsetCompression`（保持 EXPLICIT 模式、清 type）；非 EXPLICIT 下一切 type/quality 访问 ISE。

## 2. Java 用户如何使用
`ImageWriteParam p = writer.getDefaultWriteParam(); if (p.canWriteCompressed()) { p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); p.setCompressionType("Deflater"); p.setCompressionQuality(0.8f); } writer.write(null, iioImage, p);`

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/ImageWriteParam.java`：能力检查顺序为「先 canXxx 后模式」，故无能力时连非法模式参数也抛 UOE（不是 IAE）；`unsetCompression` 只清 `compressionType=null` 不动模式；再次进入 EXPLICIT 清 type。
- `com/sun/imageio/plugins/png/PNGImageWriteParam.java`（JDK-8）：`canWriteCompressed=false`、`canWriteProgressive=true` —— JDK-9 起 PNG 支持压缩质量，`canWriteCompressed=true`（版本差异，测试两侧各自断言）。
- `test/jdk/javax/imageio/plugins/png/WriteProgressive.java` 等用 `setProgressiveMode(MODE_DEFAULT)`。

## 4. 测试用例设计 + 自动化实现（`ImageWriteParamTest`）
- 结构：protected 双构造器（含 null Locale 容忍）、MODE 常量取值 0/1/2/3。
- 无能力参数（new ImageWriteParam）：约 25 个 UOE 探针（含 setTiling/getPreferredTileSizes/setCompressionQuality/getCompressionQualityDescriptions 等），且非法模式值也 UOE（优先级：能力检查在参数校验之前）。
- 平铺状态机：模式 -1/4 IAE；DISABLED 下 setTiling/getTileWidth/unsetTiling ISE；EXPLICIT 下 w<0/h=0 IAE；`NoOffsetTilesParam`（canOffsetTiles=false）非零偏移 UOE、零偏移合法；`PreferredTilesParam` 断言 getPreferredTileSizes 返回副本。
- 渐进：EXPLICIT IAE、模式 7/-1 IAE、DISABLED/DEFAULT/COPY 往返。
- 压缩状态机：模式 -1/4 IAE；非 EXPLICIT 下 set/getCompressionType、unsetCompression、getCompressionQuality 全 ISE；getCompressionTypes 副本可变不影响内部；质量 0.0/1.0 边界合法、超界 IAE；getBitRate 质量超界 IAE；setCompressionType(null) 清空；unsetCompression 保持 EXPLICIT 并清 type、随后 getCompressionQuality ISE；重新进入 EXPLICIT 清 type。
- `FreeTypeParam`（可压缩但无类型表）：setCompressionType UOE、quality 合法（无类型表不要求 type）。
