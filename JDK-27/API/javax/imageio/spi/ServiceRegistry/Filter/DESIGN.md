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

## 5. 契约深化补充（本轮）
- 精确签名：恰 1 个声明方法 `filter`；返回 boolean；单 `Object` 参数；public abstract；无受检异常；lambda SAM 可用。
- 调用语义（私有注册表 + 3 个独立叶子类）：getServiceProviders 返回时 filter 已跑 ≥1 次（迭代器预取）；排空后每提供者恰好一次（IdentityHashMap 计数）；仅被接受者可迭代。
- useOrdering 无关性：acceptAll 在 false/true 两态成员集合相同；true 时尊重偏序。
- 负例：null filter 两态均 `NullPointerException`；throwing filter 异常实例原样中继（`==`）；rejectAll 空迭代不抛。
