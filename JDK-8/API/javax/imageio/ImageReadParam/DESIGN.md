# javax.imageio.ImageReadParam — DESIGN

## 1. API 功能
`ImageReadParam` 扩展 `IIOParam`，增加读取侧参数：目标图像、目标波段、逐 pass 渐进、渲染尺寸、源渐进 pass 区间。方法分组：
- 目标：`setDestination(BufferedImage)`/`getDestination()`（直接引用，null 合法）；`setDestinationType(ImageTypeSpecifier)`（清 destination 与 destinationBands）；`setDestinationBands(int[])`（clone，负值/重复 IAE，空数组合法）。
- 渐进 pass：`setSourceProgressivePasses(int minPass, int numPasses)`（minPass<0、numPasses<0、两者相加溢出均 IAE；numPasses = MAX_VALUE 表示到最后一个 pass；getSourceMaxProgressivePass = minPass+numPasses-1）。
- 渲染尺寸：`setSourceRenderSize(Dimension)`/`canSetSourceRenderSize()`（基类恒 false，setSourceRenderSize(null) 也抛 UOE）。
- 默认值：destination/destinationBands/sourceRenderSize/destinationOffset 均为 null/零，pass 区间 0..MAX_VALUE。

## 2. Java 用户如何使用
`ImageReadParam p = reader.getDefaultReadParam(); p.setSourceRegion(...); p.setDestinationType(spec); p.setSourceSubsampling(2,2,0,0); BufferedImage bi = reader.read(0, p);`。控制渐进解码：`p.setSourceProgressivePasses(1, 3);`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/ImageReadParam.java`：`setDestinationType` 覆盖里总是 `setDestination(null)`（即使传 null 也清空 destination，与 javadoc 描述的“仅合法值才清”有出入）；`setSourceProgressivePasses` 用 `minPass + numPasses - 1` 判断溢出。
- `ImageReader.computeRegions` 消费 region/destinationOffset：目标矩形平移 destinationOffset，region 裁剪。
- `test/jdk/javax/imageio/ImageReadParamPasses.java`：验证 pass 参数边界。

## 4. 测试用例设计 + 自动化实现（`ImageReadParamTest`）
- 结构：非抽象具体类、protected 无参构造、新增字段/方法集合。
- destination 契约：直接引用（非 clone）、null 清空、与 destinationType 的联动（设 type 即清 destination；setDestinationType(null) 也清 destination —— 实测怪癖）。
- destinationBands：clone、空数组合法、负值/重复 IAE。
- setSourceRenderSize：canSetSourceRenderSize 恒 false，即使 null 参数也 UOE。
- 渐进 pass：默认 0..MAX_VALUE、往返、minPass<0 IAE、numPasses<0 IAE、minPass+numPasses 溢出 IAE、(MAX_VALUE,1) 合法边界、getSourceMaxProgressivePass = min+num-1。
- 端到端 PNG：`reader.read(0, param.setDestination(bi))` 时返回同一实例；region+子采样 1x1 时读出的像素来自 region 中心（用 (1,1) 2x2 region 内已知像素验证）。
- 白盒：方法/字段清单存在性。
