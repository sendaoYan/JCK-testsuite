# javax.imageio.spi.ServiceRegistry.Filter — DESIGN

## 1. API 功能
`ServiceRegistry.Filter` 是 `ServiceRegistry` 的嵌套函数式接口，只有一个方法：
- `boolean filter(Object provider)`：判断给定提供者是否匹配条件。

用于 `ServiceRegistry.getServiceProviders(Class, Filter, boolean)` 按任意条件筛选提供者。

## 2. Java 用户如何使用
用户实现 `ServiceRegistry.Filter`（可用 lambda），例如 `provider -> ((ImageReaderSpi) provider).getFormatNames() 含 "png"`，然后传给 `IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, filter, false)` 得到只含 PNG reader SPI 的迭代器。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/spi/ServiceRegistry.java`：`getServiceProviders(Class,Filter,boolean)` 内部先取该类别全部提供者，再用 `FilterIterator` 包装并逐个调用 `filter.filter(provider)` 过滤。
- `src/java.desktop/share/classes/javax/imageio/ImageIO.java`：大量 `getImageReadersByFormatName`/`getImageWritersBySuffix` 等实现本质是遍历 SPI 并按格式名/后缀过滤，与 `Filter` 的用法同构。

## 4. 测试用例设计 + 自动化实现（`FilterTest.testBlackBox`）
- 用 lambda 定义 png 过滤器，直接调用 `filter` 断言接受 png SPI、拒绝非提供者。
- 用 `getServiceProviders(ImageReaderSpi.class, filter, false)` 过滤默认注册表，断言结果非空、每一项格式名都含 "png"、且过滤后数量小于全部 reader 数量（确实排除了非 png）。
