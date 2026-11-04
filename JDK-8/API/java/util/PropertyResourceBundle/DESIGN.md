# java.util.PropertyResourceBundle 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。用内存 InputStream/Reader 构造，无文件副作用。

## 1. API 功能

- **构造器**：`PropertyResourceBundle(InputStream)`、`PropertyResourceBundle(Reader)`。
- **查询**：`handleGetObject`、`getKeys`、`getString/getObject`、`containsKey/keySet`。

## 2. 使用方式

`new PropertyResourceBundle(new ByteArrayInputStream("k=v".getBytes(...)))` → `getString("k")`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/PropertyResourceBundle`。
- 属性值均为 String；`getString`/`getObject` 缺失 → `MissingResourceException`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | InputStream 构造 + getString/containsKey | testPropertyResourceBundle |
| P0 | Reader 构造 + keySet/getKeys | testReaderConstructor |
| P0 | null 构造 NPE | testNullConstructor |
| P0 | unicode 转义 + 缺失 MRE + getStringArray CCE | testMissingAndUnicode |
| P0 | 契约深化：转义读取/父链 keySet/IO 异常透传/UTF-8 语义 | testUtf8EncodingContract |

## 关键规范细节

- `PropertyResourceBundle((InputStream/Reader)null)` → NPE。
- 属性值都是 String，`getStringArray` → `ClassCastException`。
- `\uXXXX` 转义按 Properties 规则解码。

## 注意事项

内存流构造；无文件副作用；headless-safe 不加 `@key`。
