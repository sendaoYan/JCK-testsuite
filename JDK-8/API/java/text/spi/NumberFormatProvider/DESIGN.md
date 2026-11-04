# java.text.spi.NumberFormatProvider 测试设计案（JDK-8）

> 覆盖目标：SPI 抽象类结构契约 + 匿名子类（fake provider）行为契约，不安装真实 provider。

## 1. API 提供了哪些功能

- 抽象类 `NumberFormatProvider`（extends `java.util.spi.LocaleServiceProvider`，无公共构造器）。
- 工厂方法与 `getAvailableLocales()`/`isSupportedLocale(Locale)`（继承自基类）。

## 2. Java 用户如何使用

- locale 敏感服务通过 `LocaleServiceProviderAdapter` 查找已安装 provider；本测试以匿名子类验证接口契约本身。

## 3. 参考来源

- Oracle javadoc（java.text.spi 包）+ `LocaleServiceProvider.isSupportedLocale` 默认实现语义（扩展标记剥离）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract/extends/无公共构造/工厂方法签名 | testWhiteBox / testContractDeepening |
| P0 | fake provider 可实例化并返回可用对象 | testXxxProvider |
| P0 | 抽象结构 + isSupportedLocale 矩阵 + 四工厂返回 en_US 实例的确定性输出（1,234.567/1,235）与 parse 往返 |

## 注意事项

- isSupportedLocale(en-US-u-ca-buddhist) 为 true：默认实现剥离 BCP-47 扩展后匹配（双侧实测一致）。
- 不依赖真实 SPI 安装；纯 headless。
