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

## 契约深化（2026-11）

本轮追加 15 个 test 方法（断言 251 → 441），核心是哈希精确值、异常消息逐字锁定、
indexOf/replace/split 全形态矩阵、intern 闭环与 getBytes 字符集矩阵。

### 新增维度

- **equals/hashCode 契约**：`""`→0、`"a"`→97、`"abc"`→96354、经典碰撞对 `"Aa"`/`"BB"`→2112
  （哈希相等但 equals false）；手算 `h = 31*h + c` 公式闭环；两次调用稳定；
  `"abc".equals(StringBuilder)` false 而 `contentEquals` true 的分叉。
- **索引族负例消息逐字锁定（8 vs 27 实测差异，MODERN 分支）**：
  - JDK-8：`charAt(-1)` → `"String index out of range: -1"`；`substring(-1/4/3,2)` 均为
    `"String index out of range: -1"`（begin/end-1 算术后的差值）；`subSequence(1,4)` → `"...: 4"`。
  - JDK-27：`charAt(-1)` → `"Index -1 out of bounds for length 3"`；substring/subSequence/getChars →
    `"Range [begin, end) out of bounds for length n"`；`codePointBefore(0)` → `"Index -1 ..."`（注意是 -1 非 0）。
  - `offsetByCodePoints` 越界两侧均为**无消息**的 `IndexOutOfBoundsException`；`codePointCount`
    越界 8 侧无消息、27 侧为 Range 形式。
- **indexOf/lastIndexOf 全形态**：char/String × fromIndex（前后/负/超长全部容错不抛）；
  `"abc".indexOf("", 99) == 3`（空针 fromIndex 钳到 length）；`"aaa".indexOf("aa", 1) == 1`。
- **replace 两形态**：`replace(char,char)`/`replace(CS,CS)` 无命中**返回 this（== 实测两侧均真）**；
  `"aaa".replace("aa","b") == "ba"`（从左到右不重叠）；空针 `"aaa".replace("","-") == "-a-a-a-"`。
- **split limit 矩阵**：`"a,b,,"` limit0→2 元素 / limit -1→4（保留尾空）；`",,"` limit0→0 / -1→3；
  `"".split(",")`→`[""]`（1 元素）；`",a"`→`["","a"]`；`"\\."` 元字符。
- **matches 正则语义**：`.` 不匹配 `\n`（无 DOTALL）、内联 `(?s)` 可匹配、`"".matches("")` true。
- **trim 空白表**：trim 只去 `<= U+0020`（NUL/U+001F/VT/FF 全去）；U+2003 与 U+00A0 均不去
  （两侧一致，length 断言）；无变化返回 this。
- **intern 闭环（JLS 3.10.5）**：`"a"+"b"` 常量折叠 == "ab"；运行时拼接 != 但 intern() == "ab"；
  substring/StringBuilder 派生串 intern 同归字面量；两个运行时实例 intern 到同一实例。
- **format 矩阵（Locale.ENGLISH）**：`%05d`→"00042"、`%,d`→"1,234,567"、`%,.2f`、`%x/%X/%o`、
  `%b(null)`→"false"、`%c(65)`→"A"、`%s null`→"null"、`%%`、`%n`（只断非空）、`%-6d`、`%+d`、
  `%e`→"1.234568e+03"、`%g` 小/大值两种科学计数、`%5.3s` 截断。
- **getBytes 字符集矩阵**：UTF-8 `"中"`→3 字节首字节 0xE4（E4 B8 AD）；US-ASCII 不可映射→0x3F '?'；
  补充平面 U+1D11E→4 字节；默认字符集 ASCII 往返。
- **构造器族 round-trip**：`new String(int[]{0x1D11E})` == `"𝄞"`；
  `new String(StringBuilder)` 快照独立；ctor/valueOf/copyValueOf 三路防御拷贝。
- **regionMatches 容错**：越界一律返回 false 不抛（toffset 4/-1、组合越界）；
  `regionMatches(3,"",0,0)`（toffset==len 的零长匹配）为 true。
- **compareTo 精确差值**：`"a" vs "c"` == -2；前缀关系返回**长度差**（`"abc" vs "ab"` == 1，
  不是 'c' 的值 99——任务书原文有误，以实测为准）；`"A" vs "a"` == -32；反对称精确。
- **流内容精确**：`chars()`/`codePoints()` toArray 全等（代理对上 chars 给 UTF-16 单元
  [0xD834,0xDD1E,0x61]、codePoints 解码 [0x1D11E,0x61]）；ASCII chars 经 int[] 构造器往返。

### 易错规范细节（新增锁定）

- 无变化优化返回 this 是**规范允许且两侧一致**的可断言行为（replace 两形态/trim/concat("")/
  substring(0)/toUpperCase 无变化/valueOf(String)），已用 == 断言。
- `"".split(",")` 返回**含一个空串的数组**（长度 1），不是空数组；`",,".split(",")` 才是空数组。
- codePointBefore(0) 的 JDK-8 消息是 `"String index out of range: 0"`，JDK-27 是
  `"Index -1 out of bounds for length n"`（27 内部把 index-1 再检查）。

### 参考来源

- Oracle javadoc（String.hashCode 公式、replace 返回 this 条款、split limit 语义、regionMatches 容错）。
- JLS 3.10.5（字符串字面量 intern）、§3.10.6。
- OpenJDK String 源码：JDK-9+ 越界消息改用 StringIndexOutOfBoundsException.checkIndex/Range 形式。
- 双侧实测（/tmp 探针程序，jdk8u372-b07 vs jdk-27.0.1.0.2+1）。
