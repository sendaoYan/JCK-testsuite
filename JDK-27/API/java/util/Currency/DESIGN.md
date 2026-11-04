# java.util.Currency 测试设计

## 1. API 功能
- 工厂：`getInstance(String code)`、`getInstance(Locale)`、`getAvailableCurrencies`、`availableCurrencies`。
- 属性：`getCurrencyCode`、`getDefaultFractionDigits`、`getNumericCode`、`getNumericCodeAsString`。
- 展示：`getSymbol(Locale)`、`getDisplayName(Locale)`、`toString`。

## 2. Java 用户如何使用
- 按代码取货币：`Currency.getInstance("USD")`。
- 按区域取货币：`Currency.getInstance(Locale.US)`。
- 金额格式化：`NumberFormat.getCurrencyInstance(Locale.US)` 内部依赖货币小数位。
- 小数位：`Currency.getInstance("JPY").getDefaultFractionDigits()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/text/DecimalFormatSymbols.java`：`currencySymbol` 通过 `Currency.getInstance` 获得。
- JDK 源码 `sun/util/locale/provider/NumberFormatProviderImpl.java`：按 Locale 查货币。
- 广泛用法（电商/支付系统）：`Currency.getInstance(code)` 校验 ISO 货币码、取小数位做金额精度。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、实现 Serializable、关键方法存在。
- 黑盒（固定 Locale.US，不依赖平台默认 locale）：
  - USD 代码/小数位 2/数值码 840/`$` 符号/显示名。
  - EUR 2 位、JPY 0 位小数。
  - `getInstance(Locale.US)` == USD、`getAvailableCurrencies` 含 USD、实例缓存相等。
