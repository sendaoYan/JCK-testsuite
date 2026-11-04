# java.util.ListResourceBundle 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。经子类覆写 `getContents` 实例化。

## 1. API 功能

- **子类**：`getContents()`；`handleGetObject`、`getKeys`。
- **查询**：`getString/getObject/getStringArray`、`containsKey/keySet`。

## 2. 使用方式

子类覆写 `getContents()` 返回 `Object[][]`；`bundle.getString(key)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/ListResourceBundle`。
- `getContents()` 返回 null → NPE（懒加载，首次访问时）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getString/getObject/containsKey/getKeys | testListResourceBundle |
| P0 | getStringArray + 缺失 MRE | testStringArrayAndMissing |
| P0 | getContents() null → NPE | testNullContents |

## 关键规范细节

- `getContents()` 返回 null 或 null key/value → NPE（懒加载，首次访问时）。
- `getString`/`getObject`/`getStringArray` 缺失 → `MissingResourceException`。

## 注意事项

纯内存子类；无全局副作用；headless-safe 不加 `@key`。
