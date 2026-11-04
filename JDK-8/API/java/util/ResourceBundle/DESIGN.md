# java.util.ResourceBundle 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。经自定义子类实例化；`getBundle` 只测错误路径（不依赖外部资源）。

## 1. API 功能

- **子类**：`handleGetObject`、`getKeys`；`getString/getStringArray/getObject`、`containsKey/keySet`。
- **查询**：`getLocale()`、`getBaseBundleName()`。
- **静态**：`getBundle(...)` 多重重载、`clearCache()`。

## 2. 使用方式

子类覆写 `handleGetObject/getKeys`；`bundle.getString(key)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/ResourceBundle`。
- `getString`/`getObject`/`getStringArray` 缺失 → `MissingResourceException`；
  `getStringArray` 非数组 → `ClassCastException`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getString/getObject/containsKey/keySet | testResourceBundle |
| P0 | getStringArray + 缺失 null + MissingResourceException + CCE | testStringArrayAndMissing |
| P1 | getLocale/getBaseBundleName | testLocaleAndBaseName |
| P0 | getBundle(null) NPE / 不存在 MRE / clearCache | testGetBundleErrors |

## 关键规范细节

- `getString`/`getObject`/`getStringArray` 缺失 → `MissingResourceException`。
- `getStringArray` 对非 `String[]` 值 → `ClassCastException`。
- 自定义子类 `getBaseBundleName()` 与未设置时 `getLocale()` → null。

## 注意事项

纯内存子类；不依赖外部 bundle 资源；无全局副作用；headless-safe 不加 `@key`。
