# java.util.ResourceBundle 测试设计

## 1. API 功能
- 工厂：`getBundle(baseName[, Locale[, Control/ClassLoader/Module]])`、`clearCache`。
- 查询：`getString`、`getStringArray`、`getObject`、`containsKey`、`keySet`。
- 子类钩子：`handleGetObject`、`handleKeySet`、`getKeys`。
- 元信息：`getBaseBundleName`、`getLocale`、`setParent`。

## 2. Java 用户如何使用
- 国际化：`ResourceBundle.getBundle("messages", Locale.US).getString("greeting")`。
- 自定义子类：覆盖 `handleGetObject`/`getKeys`。
- 数组值：`getStringArray("list")`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/lang/StackWalker.java`：`ResourceBundle.getBundle` 加载本地化消息。
- JDK 源码 `java/util/spi/AbstractResourceBundleProvider.java`：服务提供方契约。
- JDK 源码 `java/util/spi/ResourceBundleProvider.java`：SPI 接口。
- 广泛用法：Spring 消息源、GUI 国际化。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言抽象类、关键方法存在。
- 黑盒（自定义子类固定内容）：
  - getString/getObject/getStringArray/containsKey/keySet 组合。
  - 缺失键抛 MissingResourceException、null 键抛 NullPointerException。
  - getBaseBundleName 非空。
