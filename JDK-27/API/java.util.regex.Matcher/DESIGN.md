# java.util.regex.Matcher 测试方案

## 1. API 功能（按方法分组）

- 匹配操作：`matches()`、`find()`、`find(int)`、`lookingAt()`。
- 结果查询：`group()`、`group(int)`、`group(String)`、`groupCount()`、`start()`、`start(int)`、`start(String)`、`end()`、`end(int)`、`end(String)`、`hasMatch()`、`toMatchResult()`、`namedGroups()`。
- 状态控制：`reset()`、`reset(CharSequence)`、`usePattern(Pattern)`、`region(int,int)`、`regionStart()`、`regionEnd()`。
- 边界模式：`useTransparentBounds(boolean)`、`hasTransparentBounds()`、`useAnchoringBounds(boolean)`、`hasAnchoringBounds()`。
- 引擎状态：`hitEnd()`、`requireEnd()`。
- 替换：`appendReplacement(StringBuffer/StringBuilder,String)`、`appendTail(...)`、`replaceAll(String)`、`replaceFirst(String)`、`replaceAll(Function)`、`replaceFirst(Function)`、`static quoteReplacement(String)`。
- 流：`results()`。
- 其他：`pattern()`、`toString()`。

## 2. Java 用户如何使用

- 构造：不能 new，只能 `Pattern.compile(regex).matcher(input)`。
- 典型调用：
  - 全量匹配 `m.matches()`；前缀匹配 `m.lookingAt()`；迭代 `while (m.find()) { m.group(i); m.start(); m.end(); }`。
  - 命名组抽取 `m.group("name")`、`m.start("name")`。
  - 限定搜索区间 `m.region(from, to)`；lookbehind 跨边界 `m.useTransparentBounds(true)`。
  - 替换：`m.replaceAll(repl)` / `m.replaceFirst(repl)`；流式 `m.results()`。
  - 手工替换模板：`while(m.find()) m.appendReplacement(sb, repl); m.appendTail(sb);`。
- 真实组合链路：
  1. `compile→matcher→find 循环→group/start/end` 抽取所有匹配。
  2. `region→find→group` 限定区间匹配。
  3. `matches/lookingAt/find(int)` 三种锚定差异。
  4. `appendReplacement/appendTail` 与 `replaceAll/replaceFirst` 的替换语义（含 `$g`、`${name}`、`$0`）。
  5. `reset/usePattern` 复用匹配器；`toMatchResult` 生成不可变快照。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.regex.Matcher.java:942-980`：`appendReplacement` javadoc 给出官方循环示例
  `Pattern p = Pattern.compile("cat"); Matcher m = p.matcher("one cat two cats in the yard");`
  `StringBuffer sb = new StringBuffer(); while (m.find()) m.appendReplacement(sb, "dog"); m.appendTail(sb);`
  得到 `one dog two dogs in the yard`。
- JDK 测试 `test/jdk/java/util/regex/RegExTest.java:320 hitEndTest`：`^squidattack` 对 `squack`/`squid`，
  以及 `\\R` 对 `\r`/`\r\n` 的 `hitEnd()` 断言（本类据此测 hitEnd/requireEnd）。
- JDK 测试 `test/jdk/java/util/regex/NamedGroupsTests.java`：`group(name)`/`start(name)`/`end(name)` 与
  `namedGroups()` 编号映射一致性。
- JDK 测试 `test/jdk/java/util/regex/ImmutableMatchResultTest.java`：`toMatchResult()` 返回的
  `MatchResult` 不受后续 `find()` 影响（不可变快照）。
- JDK 源码 `java.util.regex.Matcher.java:1547 region`、`:1633 useTransparentBounds`：region 边界
  opaque/transparent 与 anchoring 语义的文档描述。

## 4. 测试用例设计 + 自动化实现

实现位置：`MatcherTest.java`（`testWhiteBox()` 白盒 + 下列黑盒方法）。

- `testFindLoop()`：`(\\w+)=(\\d+)` 上 find 循环抽取全部 group/start/end，与预期列表比对。
- `testMatchesLookingAtFind()`：matches/lookingAt/find(int)/find(0) 三种锚定差异。
- `testGroupCountAndNamed()`：嵌套组编号、命名组 group(name)/start(name)/end(name)/namedGroups。
- `testOptionalGroupAndErrors()`：未参与组返回 null/-1；未匹配时 start/end/group 抛 IllegalStateException；
  group 越界抛 IndexOutOfBoundsException、未知命名组抛 IllegalArgumentException、null 名抛 NPE。
- `testResetAndUsePattern()`：reset()/reset(CharSequence)/usePattern 复用与 null 校验。
- `testRegion()`：region 默认/设置、find 受限、非法参数三态 IndexOutOfBoundsException。
- `testBounds()`：transparent bounds 允许 lookbehind 跨边界；anchoring bounds 决定 ^ 是否锚定 region 起点。
- `testHitEndRequireEnd()`：`a$`/`a`/`abc` 三例的 hitEnd 与 requireEnd 语义。
- `testAppendReplace()`：appendReplacement/appendTail 官方示例 + 组引用 + StringBuilder 重载 + $0。
- `testReplaceAllFirst()`：replaceAll/replaceFirst 字符串与 Function 重载、组引用、quoteReplacement 字面化。
- `testQuoteReplacement()`：quoteReplacement 转义 `$`/`\\`。
- `testResultsAndToMatchResult()`：results() 流 + toMatchResult 不可变快照。
- `testHasMatchAndToString()`：hasMatch 状态 + toString 前缀。
- 健壮性：全部固定输入与正则；无墙钟/随机/无序集合迭代（find 循环结果按顺序入列表断言）；
  无全局副作用；无文件/网络/端口；跨平台无路径/编码依赖；headless-safe 不加 `@key`。
