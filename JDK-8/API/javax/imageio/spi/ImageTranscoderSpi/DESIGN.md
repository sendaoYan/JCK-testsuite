# javax.imageio.spi.ImageTranscoderSpi — DESIGN（JDK-8）

## 1. API 功能
`ImageTranscoderSpi` 是转码插件 SPI 抽象基类（继承 `IIOServiceProvider`），抽象方法仅 3 个：
- `getReaderServiceProviderName()`、`getWriterServiceProviderName()`：声明配对的 reader/writer SPI 类名。
- `createTranscoderInstance()`：产出 `ImageTranscoder`。
- 构造器继承 `(String vendorName, String version)`，null 参数抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
`ImageIO.getImageTranscoders(ImageReader,ImageWriter)` 遍历注册表，按 reader/writer SPI 名匹配选中转码器；本类把"配对关系"与"实例化"解耦。JDK 自带注册表中无内置 transcoder SPI（迭代器为空属正常）。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/ImageTranscoderSpi.java`：完整 javadoc + 3 个抽象方法定义。
- `jdk/src/share/classes/javax/imageio/ImageIO.java`：`getImageTranscoders` 用 `ServiceRegistry.Filter` 匹配 `getReaderServiceProviderName().equals(readerSpi.getClass().getName())` 等条件。
- OpenJDK 8 主源码树不含标准 transcoder SPI 实现（仅第三方如 JPEG→PNG 转码器使用该机制）。

## 4. 测试用例设计 + 易错规范细节（`ImageTranscoderSpiTest`）
- 自写最小子类（TinyTranscoderSpi）：`(String,String)` 构造注入 vendor/version 往返；reader/writer SPI 名往返；`getDescription(Locale.US)` 与 `getDescription(null)` 一致；`createTranscoderInstance()` 返回子类指定值并计数分派；`instanceof IIOServiceProvider`/`RegisterableService` 层级断言。
- 构造校验：null vendor、null version 均抛 `IllegalArgumentException`（继承自基类，精确类实测）。
- 注册回调：私有注册表（类别仅 ImageTranscoderSpi）注册 → onRegistration 一次、`contains`/`getServiceProviderByClass` 身份 → 注销 → onDeregistration 一次、不可见。
- 全局注册表迭代器：非 null；JDK 无内置 transcoder 时优雅降级（不硬断言非空）。
- 易错点：`ImageTranscoderSpi` 是允许出现在 `ServiceRegistry` 类别中的 5 个标准类别之一，自写子类注册无需额外配置；配对名比较用字符串全限定名（非 Class 对象）。
