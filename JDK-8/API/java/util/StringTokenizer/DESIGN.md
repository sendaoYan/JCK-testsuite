# java.util.StringTokenizer 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。纯内存，无全局副作用。

## 1. API 功能

- **构造器**：`StringTokenizer(String)`、`StringTokenizer(String,String)`、
  `StringTokenizer(String,String,boolean)`。
- **token**：`hasMoreTokens()`、`nextToken()`、`nextToken(String)`、`countTokens()`。
- **Enumeration**：`hasMoreElements()`、`nextElement()`。

## 2. 使用方式

`new StringTokenizer(s, delim)` → `hasMoreTokens()`/`nextToken()`；`nextToken(newDelim)` 切换分隔符；
`returnDelims=true` 时返回分隔符本身。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/StringTokenizer`（legacy；建议用 String.split/Scanner）。
- 空源无 token；无分隔符出现时整个字符串是一个 token。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基础 token + 耗尽 NSEE | testTokens |
| P0 | 默认空白分隔符 | testDefaultDelimiter |
| P0 | returnDelims=true 返回分隔符 | testReturnDelims |
| P0 | nextToken(delim) 切换分隔符 | testNextTokenSwitchDelim |
| P0 | Enumeration hasMoreElements/nextElement + 耗尽 | testEnumeration |
| P1 | 空串无 token + 无分隔符整串一个 token | testEmptyAndNoDelims |
| P0 | 契约深化：- | testContracts |

## 关键规范细节

- `nextToken()`/`nextElement()` 耗尽 → `NoSuchElementException`。
- 默认构造器按任意空白（`\t\n\r\f` + 空格）切分。
- `nextToken(String)` 切换后续 token 的分隔符集合。

## 注意事项

纯内存；无全局副作用；headless-safe 不加 `@key`。
