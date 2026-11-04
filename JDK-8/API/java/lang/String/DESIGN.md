# java.lang.String 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「方法被调用一次」的冒烟。四类历史盲区——构造器/工厂参数
> 边界与精确异常、null 歧义语义、equals/hashCode/compareTo 契约、Unicode 代理对与索引边界
> 交叉场景——全部补齐。断言不依赖 Locale/Charset/墙钟，headless 安全。

## 1. API 提供了哪些功能

- **构造器（~15）**：`String()`、`String(String)`、`String(char[])`、`String(char[],off,len)`、
  `String(byte[])`、`String(byte[],off,len)`、`String(byte[],Charset)`、`String(byte[],String)`
  （+ off/len 变体）、`String(int[],off,len)`、`String(StringBuilder)`、`String(StringBuffer)`。
- **查询**：`length`/`isEmpty`/`charAt`/`codePointAt`/`codePointBefore`/`codePointCount`/
  `offsetByCodePoints`/`getChars`/`getBytes`(×4)/`toCharArray`/`contentEquals`(×2)。
- **比较**：`equals`/`equalsIgnoreCase`/`compareTo`/`compareToIgnoreCase`/`regionMatches`(×2)。
- **前缀/后缀/搜索**：`startsWith`/`endsWith`/`indexOf`/`lastIndexOf`(char/int/String 重载)。
- **切片/拼接**：`substring`(×2)/`subSequence`/`concat`。
- **替换/正则**：`replace(char,char)`/`replace(CharSequence,CharSequence)`/`replaceAll`/
  `replaceFirst`/`matches`/`split`(×2)。
- **大小写/空白**：`toLowerCase`/`toUpperCase`(各 ×2 Locale 重载)/`trim`。
- **静态工厂/工具**：`valueOf`(×9)/`copyValueOf`(×2)/`intern`/`join`(×2)/`format`(×2)/
  `chars`/`codePoints`。

## 2. Java 用户如何使用

- `new String(char[], off, len)` 从字符数组切片建串；`new String(byte[], Charset)` 解码字节；
  `new String(int[], off, len)` 从码点数组（含代理对）建串。
- `equalsIgnoreCase` 比较大小写不敏感文本；`compareTo` 做字典序排序；`regionMatches` 做局部比较。
- `indexOf`/`lastIndexOf` 搜索字符或子串（int 重载把 int 当完整码点，能命中补充平面代理对）。
- `replaceAll`/`split`/`matches` 用正则；`join` 连接集合；`format` 格式化。
- 依赖 `valueOf((Object)null) == "null"`、`equals(null) == false`、`equalsIgnoreCase(null) == false`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/String.html`）：构造器/异常契约、null 语义、
  `indexOf(int)` 的补充平面码点行为、`contentEquals`、`split` 的 limit 语义。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/String.java`：
  `codePointCount`/`offsetByCodePoints` 抛 `IndexOutOfBoundsException`（非 SIOOBE）、
  `getChars` 抛 SIOOBE、`String(char[],off,len)` 允许 `offset == length` 且 `count == 0`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/String` + `~/git/jdk/test/jdk/java/lang/String`：
  代理对边界、`indexOf` 码点、`trim`、`format`、`contentEquals`、`compareTo` 历史缺陷回归。

## 4. 测试用例设计（映射到 StringTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：15 个重载全走 + null→NPE + 负/越界→SIOOBE/IOOBE + 码点非法→IAE + 未知 charset→UnsupportedEncodingException + 防御拷贝 | testConstructorMatrix |
| P0 | length/charAt/代理对单元 + charAt 越界 | testLengthCharAt |
| P0 | 码点家族 + 代理对 + codePointCount/offsetByCodePoints 越界（IOOBE，非 SIOOBE） | testCodePoints |
| P0 | getChars/getBytes/toCharArray + 防御拷贝 + 越界 + 未知 charset + null Charset | testGetCharsBytes |
| P0 | equals 契约（自反/对称/传递/一致/跨类型/null）+ hashCode + compareTo + equalsIgnoreCase(null)→false + compareTo(null)→NPE | testEqualityCompare |
| P0 | regionMatches 精确/忽略大小写/空串/越界 + null→NPE | testRegionMatches |
| P0 | startsWith/endsWith + 空串 + 边界 + null→NPE | testStartsEnds |
| P0 | indexOf/lastIndexOf char/int/String + 空串 + 负 fromIndex + 代理对 + null→NPE | testIndexOf |
| P0 | substring/subSequence/concat + 全边界越界 + concat(null)→NPE | testSubstringConcat |
| P1 | contentEquals(CharSequence/StringBuffer) + null→NPE | testContentEquals |
| P1 | replace 四重载 + 正则 + null→NPE | testReplace |
| P1 | matches/split + limit 语义（-1/2/默认） + 空串 + null→NPE | testMatchesSplit |
| P1 | 大小写转换 + Turkish-I 边界 + null Locale→NPE | testCase |
| P1 | trim（仅 ≤0x20）/intern 恒等 | testTrimIntern |
| P1 | valueOf/copyValueOf 全重载 + null→NPE + 越界→IOOBE | testValueOf |
| P1 | contains/join/format + null→NPE | testContainsJoinFormat |
| P2 | chars/codePoints 流（UTF-16 单元 vs 码点计数） | testStreams |

## 关键规范细节（易错，已锁入断言）

- **`indexOf(int)`/`lastIndexOf(int)` 把 int 当完整码点**：`"a" + 补充平面字符` 的
  `indexOf(0x1D518) == 1`（命中代理对），而 `indexOf((char)0x1D518) == -1`（截断成孤立 char）。
- **`codePointCount`/`offsetByCodePoints` 抛 `IndexOutOfBoundsException`**（基类），其余越界
  （charAt/codePointAt/codePointBefore/getChars/substring）抛 `StringIndexOutOfBoundsException`。
- **`String(char[], off, len)` 允许 `off == length` 且 `len == 0`**（空串），`off > length` 才抛异常。
- **null 歧义**：`equals(null)`/`equalsIgnoreCase(null)` 返回 `false`（不抛）；`compareTo(null)`/
  `contains(null)`/`concat(null)`/`replace(null,…)`/`split(null)`/`matches(null)`/
  `startsWith(null)`/`endsWith(null)`/`regionMatches(null)`/`toLowerCase(null Locale)`/
  `format(null)`/`join(null,…)`/`valueOf((char[])null)` → NPE；`valueOf((Object)null)` → `"null"`。
- **防御拷贝**：`String(char[])`/`toCharArray()`/`getChars` 拷贝而非共享；`String(StringBuilder)`
  拷贝当前内容（后续修改不影响）。
- **`getBytes(String)`/`String(byte[],String)` 抛受检 `UnsupportedEncodingException`**，不是运行时异常。

## 注意事项

- 不依赖默认 Locale/Charset：显式用 `StandardCharsets.UTF_8` / `Locale.US` / `Locale.forLanguageTag`。
- 不依赖墙钟/随机/文件/网络；headless 安全，不加 `@key`。
- `String.valueOf(Object)` 断言用同一实例对比 `obj.toString()`，避免非确定。
