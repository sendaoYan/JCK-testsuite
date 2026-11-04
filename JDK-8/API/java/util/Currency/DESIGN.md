# java.util.Currency 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。用固定 ISO 代码（USD/JPY）与固定 Locale.US 保证确定性。

## 1. API 功能

- **工厂**：`getInstance(String)`、`getInstance(Locale)`、`getAvailableCurrencies()`。
- **属性**：`getCurrencyCode()`、`getDefaultFractionDigits()`、`getNumericCode()`、
  `getSymbol()/getSymbol(Locale)`、`getDisplayName()/getDisplayName(Locale)`、`toString()`。
- **序列化**：`Serializable`（readResolve 缓存实例）。

## 2. 使用方式

`Currency.getInstance("USD")`；`Currency.getInstance(Locale.US)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Currency`。
- JDK 8 源码：非法/空代码 → IAE；null → NPE；代码大小写敏感（小写/混合大小写 → IAE）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | USD 基本 + Locale | testUsd / testFromLocale |
| P0 | getInstance null NPE / 非法 IAE | testGetInstanceErrors |
| P0 | 大小写敏感（小写/混合 → IAE） | testCaseSensitive |
| P0 | numericCode/fractionDigits/symbol/displayName/toString | testAttributes |
| P1 | getAvailableCurrencies | testAvailableCurrencies |
| P1 | Serializable round-trip 缓存实例 | testSerialization |

## 关键规范细节

- `getInstance(null)` → NPE；`getInstance("")`/非法代码 → IAE。
- USD numericCode=840、fractionDigits=2；JPY fractionDigits=0、numericCode=392。
- 反序列化返回缓存实例（`==`）。

## 注意事项

固定 ISO 代码/Locale.US；无全局副作用；headless-safe 不加 `@key`。
