# java.util.regex.Matcher 测试设计案（JDK-8）

> 覆盖目标：完整契约（三种匹配锚定、分组/命名组、null/异常、region/边界、替换、快照），非冒烟。

## 1. API 提供了哪些功能

- 匹配：`matches()`、`find()`、`find(int)`、`lookingAt()`。
- 结果查询：`group()`、`group(int)`、`group(String)`、`groupCount()`、`start()`、`start(int)`、
  `start(String)`、`end()`、`end(int)`、`end(String)`、`toMatchResult()`。
- 状态控制：`reset()`、`reset(CharSequence)`、`usePattern(Pattern)`、`region(int,int)`、
  `regionStart()`、`regionEnd()`。
- 边界模式：`useTransparentBounds`/`hasTransparentBounds`、`useAnchoringBounds`/`hasAnchoringBounds`。
- 引擎状态：`hitEnd()`、`requireEnd()`。
- 替换：`appendReplacement(StringBuffer,String)`、`appendTail(StringBuffer)`、`replaceAll(String)`、
  `replaceFirst(String)`、静态 `quoteReplacement(String)`。
- `final` 类、实现 `MatchResult`、无公开构造器（只能经 `Pattern.matcher` 获取）。
- JDK-8 无：`hasMatch`/`namedGroups`/`results`/`replaceAll(Function)`/`replaceFirst(Function)`/
  `appendReplacement(StringBuilder,...)`/`appendTail(StringBuilder)`。

## 2. Java 用户如何使用

- `Pattern.compile(regex).matcher(input)` → `while(m.find()) { m.group(i); m.start(); m.end(); }`。
- `matches()` 全量、`lookingAt()` 前缀、`find(int)` 从指定位置。
- `region(from,to)` 限区间；`useTransparentBounds(true)` 允许 lookbehind 跨边界。
- 替换：`m.replaceAll(repl)`/`replaceFirst(repl)`；手工 `appendReplacement/appendTail`。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc（`java/util/regex/Matcher.html`，appendReplacement 官方循环示例）。
- OpenJDK 回归 `~/git/jdk8u-dev/jdk/test/java/util/regex/RegExTest.java`（hitEnd 等）。
- JDK-27 同目录 DESIGN.md（剔除 JDK-27 特有方法）。

## 4. 测试用例设计（映射到 MatcherTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、实现 MatchResult、无公开构造器、公开方法形态（仅 JDK-8 集合） | testWhiteBox |
| P0 | find 循环抽取 group/start/end 顺序断言 | testFindLoop |
| P0 | matches/lookingAt/find(int)/find(0) 锚定差异 | testMatchesLookingAtFind |
| P0 | 嵌套组 + 命名组 group(name)/start(name)/end(name) | testGroupCountAndNamed |
| P0 | 未参与组 null/-1；匹配前 group/start/end→ISE；越界→IOOBE；未知名→IAE；null 名→NPE | testOptionalGroupAndErrors |
| P0 | reset()/reset(CharSequence)/usePattern + null→NPE/IAE | testResetAndUsePattern |
| P0 | region 默认/设置/受限 find + 非法参数三态→IOOBE | testRegion |
| P0 | transparent/anchoring bounds 语义 | testBounds |
| P1 | hitEnd/requireEnd 三例 | testHitEndRequireEnd |
| P1 | appendReplacement/appendTail（StringBuffer）+ 组引用 + $0 | testAppendReplace |
| P1 | replaceAll/replaceFirst + $ 组引用 + quoteReplacement 字面化 | testReplaceAllFirst、testQuoteReplacement |
| P1 | toMatchResult 不可变快照；零宽匹配/空 region/反向引用/toString | testToMatchResult、testBoundaryEmptyAndEscapes |

## 关键规范细节（易错，已锁入断言）

- 未参与捕获组 `group(i)==null`、`start(i)==end(i)==-1`。
- 未匹配先查 `group()/start()/end()` 抛 `IllegalStateException`；组索引越界抛 `IndexOutOfBoundsException`；
  未知命名组抛 `IllegalArgumentException`；null 名抛 `NullPointerException`。
- `appendReplacement` 替换串中 `$` 与 `\` 是元字符；`quoteReplacement` 可将其字面化。
- `toMatchResult()` 返回独立快照，不随后续 `find()` 改变。

## 注意事项

- 固定输入/正则；find 循环结果按顺序入 List 断言；无默认 Locale/墙钟/随机；headless-safe 不加 `@key`。
