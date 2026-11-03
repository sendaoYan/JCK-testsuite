# javax.imageio.ImageReadParam — DESIGN

## 1. API 功能
`ImageReadParam` 继承 `IIOParam`，控制解码。方法分组：
- 目标图：`setDestination(BufferedImage)`/`getDestination`/`setDestinationType`（设类型会清空目标图）。
- 目标波段：`setDestinationBands(int[])`/`getDestinationBands`（clone，拒绝负/重复）。
- 渲染尺寸：`canSetSourceRenderSize`/`setSourceRenderSize`/`getSourceRenderSize`。
- 渐进式：`setSourceProgressivePasses(int,int)`/`getSourceMinProgressivePass`/`getSourceMaxProgressivePass`/`getSourceNumProgressivePasses`。

## 2. Java 用户如何使用
`ImageReader r = ...; ImageReadParam p = r.getDefaultReadParam(); BufferedImage dst = new BufferedImage(w,h,TYPE_INT_ARGB); p.setDestination(dst); BufferedImage out = r.read(0,p); // out == dst`。也可 `p.setSourceSubsampling(2,2,0,0)` 做降采样解码。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `test/jdk/javax/imageio/ImageReadParamPasses.java`：验证 `setSourceProgressivePasses(minPass,numPasses)` 与 `getSourceMin/Num/MaxProgressivePass` 的边界（含 `Integer.MAX_VALUE` 与回绕 IAE）。
- `src/java.desktop/share/classes/javax/imageio/ImageReadParam.java:200`：`setDestinationType` 先 `super.setDestinationType` 再 `setDestination(null)`。
- `test/jdk/javax/imageio/ImageReaderReadAll.java`：`readAll(0, irp)` 组合使用 `ImageReadParam`。

## 4. 测试用例设计 + 自动化实现（`ImageReadParamTest.testBlackBox`）
- 默认值：destination/destinationBands/sourceRenderSize 为 null，canSetSourceRenderSize=false，渐进默认 0/MAX/MAX。
- destination 引用身份、setDestinationType 清空 destination。
- destinationBands clone/null/重复/负值异常。
- setSourceRenderSize 默认抛 UnsupportedOperationException。
- setSourceProgressivePasses 往返、MAX 语义、负/零参数抛 IAE。
- 继承 IIOParam 的 setSourceSubsampling 往返。
- 端到端：真实 PNG 读入指定 destination，像素写入目标图。
- 白盒：extends IIOParam + 全部方法存在。
