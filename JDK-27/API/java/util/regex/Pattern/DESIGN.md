# java.util.regex.Pattern 测试方案

## 1. API 功能（按方法分组）

- 工厂/编译：`compile(String)`、`compile(String, int flags)`、`static matches(String, CharSequence)`。
- 查询：`pattern()`、`flags()`、`toString()`、`matcher(CharSequence)`、`namedGroups()`。
- 拆分：`split(CharSequence)`、`split(CharSequence, int limit)`、`splitWithDelimiters(CharSequence, int limit)`、`splitAsStream(CharSequence)`。
- 字面化：`static quote(String)`。
- 谓词/流桥接：`asPredicate()`（find 语义）、`asMatchPredicate()`（matches 语义）、`splitAsStream`。
- 标志常量：`UNIX_LINES`、`CASE_INSENSITIVE`、`COMMENTS`、`MULTILINE`、`LITERAL`、`DOTALL`、`UNICODE_CASE`、`CANON_EQ`、`UNICODE_CHARACTER_CLASS`。

## 2. Java 用户如何使用

- 构造：`Pattern p = Pattern.compile("\\d+")`（预编译复用）或 `Pattern.compile("x", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)`。
- 典型调用：
  - 一次性匹配：`Pattern.matches(regex, input)`。
  - 分词：`Pattern.compile("[,;]").split(line)`、`splitAsStream`。
  - 保留分隔符：`splitWithDelimiters(input, -1)`。
  - 匹配器：`p.matcher(input)` 得到 `Matcher` 再 `find()/group()`。
  - 字面量匹配：`Pattern.compile(Pattern.quote(userInput))`。
  - 流式过滤：`lines.filter(p.asPredicate())`。
- 真实组合链路：
  1. `compile(regex, flags)` → `pattern()/flags()/toString()` 校验回读。
  2. `matcher(input)` → `find()/group(1)/start/end` 组合抽取。
  3. `split(input, limit)` 各 limit 语义（正/零/负）→ `splitWithDelimiters` 交替子串与分隔符。
  4. `quote` → `compile(quoted)` 精确字面匹配。
  5. `asPredicate/asMatchPredicate/splitAsStream` 与 Stream/Predicate 组合。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.regex.Pattern` 自身：`Pattern.java:1220` `matches` 实现为
  `Pattern.compile(regex).matcher(input).matches()`；`Pattern.java:1499` `quote` 用 `\Q...\E`
  包裹并在出现 `\E` 时 `\E\\E\Q` 转义。
- JDK 测试 `test/jdk/java/util/regex/PatternStreamTest.java`：对 `Pattern.compile(":").splitAsStream(...)`
  与 `split` 的结果逐元素比对，覆盖 `count()`、`toArray()` 终端操作。
- JDK 测试 `test/jdk/java/util/regex/SplitWithDelimitersTest.java`：`splitWithDelimiters` 的
  `boo:::and::foo` / `:+` 文档示例（limit 2/5/-1）逐项断言。
- JDK 测试 `test/jdk/java/util/regex/NamedGroupsTests.java`：`namedGroups()` 返回的名称→编号映射
  与 `group(name)`/`start(name)` 一致性。
- JDK 测试 `test/jdk/java/util/regex/RegExTest.java:875`：`matcher.hitEnd()` 与 `\\R` 行终止符
  命中行为（本类不直接测 hitEnd，交由 MatcherTest 覆盖）。

## 4. 测试用例设计 + 自动化实现

实现位置：`PatternTest.java`（`testWhiteBox()` 白盒 + 下列黑盒方法）。

- `testCompileFlags()`：compile 回读 pattern/toString/flags；组合标志 flags() 精确返回；
  UNICODE_CHARACTER_CLASS 隐式 UNICODE_CASE；未知标志位抛 IllegalArgumentException。
- `testMatchesConvenience()`：`Pattern.matches` 一次性匹配正/反例与空串。
- `testMatcherGroups()`：compile→matcher→find→group(0/1/2)/start/end/groupCount 组合链路。
- `testFlagsBehavior()`：LITERAL / CASE_INSENSITIVE / MULTILINE / DOTALL / COMMENTS /
  UNIX_LINES / UNICODE_CHARACTER_CLASS 各标志的可观察行为差异（含 \r、\n、非 ASCII 数字）。
- `testSplit()`：split 三种 limit 语义 + trailing empty 丢弃/保留 + 无匹配单元素。
- `testSplitWithDelimiters()`：交替子串/分隔符，limit 0 丢尾部空串。
- `testQuote()`：quote 返回 `\Q...\E` 且编译后精确字面匹配、禁用元字符。
- `testNamedGroups()`：namedGroups 映射内容、空映射、不可修改。
- `testPredicatesAndStream()`：asPredicate(find)/asMatchPredicate(matches)/splitAsStream 内容与 count。
- `testEdgePatterns()`：空串、量词、内联标志、词边界等边界。
- 健壮性：全部固定字符串与正则；无墙钟/随机/无序集合迭代（namedGroups 用 size+get，不迭代顺序）；
  无全局副作用；无文件/网络/端口；跨平台无路径/编码依赖；headless-safe 不加 `@key`。
