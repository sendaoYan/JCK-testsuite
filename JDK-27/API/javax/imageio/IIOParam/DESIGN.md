# javax.imageio.IIOParam — DESIGN

## 1. API 功能
`IIOParam` 是 `ImageReadParam`/`ImageWriteParam` 的抽象基类，承载读写共用的参数：源区域、子采样、源波段、目标类型、目标偏移、控制器。方法分组：
- 源区域：`setSourceRegion(Rectangle)`/`getSourceRegion()`（内部 clone）。
- 子采样：`setSourceSubsampling(int,int,int,int)` 与四个 getter。
- 波段：`setSourceBands(int[])`/`getSourceBands()`（内部 clone，拒绝负值与重复）。
- 目标类型/偏移：`setDestinationType(ImageTypeSpecifier)`/`getDestinationType`、`setDestinationOffset(Point)`/`getDestinationOffset`（clone，null 抛 IAE）。
- 控制器：`setController/getController/getDefaultController/hasController/activateController`。

## 2. Java 用户如何使用
用户通过具体子类 `ImageReadParam`/`ImageWriteParam` 使用：`ImageReadParam p = reader.getDefaultReadParam(); p.setSourceRegion(new Rectangle(0,0,100,100)); p.setSourceSubsampling(2,2,0,0); reader.read(0,p);`。控制器通过 `p.setController(c); boolean ok = p.activateController();` 触发参数采集。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/ImageReadParam.java:200`：`setDestinationType` 调用 `super.setDestinationType(...)` 后清空 destination（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java` 内多处调用 `param.setSourceRegion`/`setSourceSubsampling` 的 `getSourceRegion`/`computeRegions`（源码内）。
- `test/jdk/javax/imageio/ImageReadParamPasses.java`：验证 `setSourceProgressivePasses` 边界，属同一参数家族。

## 4. 测试用例设计 + 自动化实现（`IIOParamTest.testBlackBox`）
- 默认值：region/bands/destinationType/controller 为 null，子采样 1/1/0/0，destinationOffset (0,0)。
- sourceRegion clone 语义（改原对象不影响已设值）、null 清空、非法 region 抛 IAE。
- setSourceSubsampling 往返、非法周期/偏移抛 IAE。
- setSourceBands clone、null、重复/负值抛 IAE。
- destinationType 引用身份、destinationOffset clone + null 抛 IAE。
- 控制器：false/true 控制器委托、hasController、接收的参数身份、null 后 activateController 抛 IllegalStateException。
- 白盒：abstract + protected 构造器 + 全部公共方法存在。
