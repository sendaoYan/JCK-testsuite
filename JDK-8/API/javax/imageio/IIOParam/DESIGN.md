# javax.imageio.IIOParam — DESIGN

## 1. API 功能
`IIOParam` 是 `ImageReadParam`/`ImageWriteParam` 的抽象基类，承载读写共用参数：源区域、子采样、源波段、目标类型、目标偏移、控制器。方法分组：
- 源区域：`setSourceRegion(Rectangle)`/`getSourceRegion()`（内部 clone）。
- 子采样：`setSourceSubsampling(int,int,int,int)` 与 `getSourceXSubsampling` 等四个 getter。
- 波段：`setSourceBands(int[])`/`getSourceBands()`（内部 clone，拒绝负值与重复；空数组合法）。
- 目标类型/偏移：`setDestinationType(ImageTypeSpecifier)`、`setDestinationOffset(Point)`（clone，null 抛 IAE；负偏移合法）。
- 控制器：`setController/getController/getDefaultController/hasController/activateController`。

## 2. Java 用户如何使用
用户通过具体子类使用：`ImageReadParam p = reader.getDefaultReadParam(); p.setSourceRegion(new Rectangle(0,0,100,100)); p.setSourceSubsampling(2,2,0,0); reader.read(0,p);`。控制器：`p.setController(c); boolean ok = p.activateController();`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/IIOParam.java`：`setSourceRegion` 检查 `x/y < 0 || w/h <= 0` 抛 IAE 后 `(Rectangle)sourceRegion.clone()`；`setSourceSubsampling` 检查周期 ≥1 且偏移在 [0, period-1]；`activateController`（行 670）在无控制器时抛 `IllegalStateException("hasController() == false!")`。
- `ImageReader.checkReadParamBandSettings`：以 `getSourceBands/getDestinationBands` 为输入做数量一致性校验。
- `test/jdk/javax/imageio/GetReaderWriterInfoTest.java` 等遍历插件默认参数，覆盖 `IIOParam` 默认状态面。

## 4. 测试用例设计 + 自动化实现（`IIOParamTest`）
- 默认值：region/bands/destinationType/controller 为 null，子采样 1/1/0/0，destinationOffset (0,0)。
- region 校验矩阵（精确类断言）：`x<0/y<0/w<=0/h<=0` 全部 IAE；超出源尺寸的 region 被接受（延迟到 reader 校验）；clone 语义；null 清空。
- 子采样：往返、周期 0/负数 IAE、偏移 = period 抛 IAE（边界 [0, period-1]）；region+子采样跨状态互不干扰。
- 波段：clone、null 清空、空数组合法、重复/负值 IAE。
- 目标类型/偏移：引用身份、clone、null Point 抛 IAE、负偏移合法。
- 控制器：异常透传（RuntimeException 连消息带出 activateController）、defaultController 不被隐式调用（通过子类注入 protected 字段探测）。
- 状态污染：各 setter 独立，无串扰。
- 白盒：abstract + protected 构造器 + 全部公共方法存在。
