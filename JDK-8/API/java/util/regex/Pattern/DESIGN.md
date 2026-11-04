# java.util.regex.Pattern 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造/编译异常、null 语义、正则语义、边界交叉、序列化），非「方法被调用一次」的冒烟。

## 1. API 提供了哪些功能

- 静态工厂：`compile(String)`、`compile(String,int)`。
- 查询：`pattern()`、`toString()`、`flags()`、`matcher(CharSequence)`。
- 便捷匹配：静态 `matches(String,CharSequence)`。
- 拆分：`split(CharSequence)`、`split(CharSequence,int)`；`splitAsStream(CharSequence)`。
- 字面化：静态 `quote(String)`；谓词 `asPredicate()`。
- 静态标志常量：`UNIX_LINES`/`CASE_INSENSITIVE`/`COMMENTS`/`MULTILINE`/`LITERAL`/`DOTALL`/
  `UNICODE_CASE`/`CANON_EQ`/`UNICODE_CHARACTER_CLASS`。
- `final` 类、实现 `Serializable`（serialVersionUID == 5073258162644648461L）。

## 2. Java 用户如何使用

- `Pattern.compile(regex)` 编译 → `p.matcher(input)` 得到 Matcher → `find/matches` 循环。
- `Pattern.matches(regex, input)` 一次性全量匹配；`p.split(s)` 按正则拆分。
- `Pattern.quote(s)` 把含元字符的字符串转义为字面量；`p.asPredicate()` 做 find 谓词。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc（`java/util/regex/Pattern.html`）。
- OpenJDK 回归 `~/git/jdk8u-dev/jdk/test/java/util/regex`（RegExTest 等）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/Pattern`。
- JDK-27 同目录 DESIGN.md（JDK-27 特有 API 已剔除：namedGroups/asMatchPredicate/splitWithDelimiters）。

## 4. 测试用例设计（映射到 PatternTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、Serializable、9 个静态标志、公开方法形态（含 JDK-8 的 asPredicate/splitAsStream，不含 JDK-27 特有） | testWhiteBox |
| P0 | compile 异常：坏 pattern→PatternSyntaxException；compile(null)→NPE；UNICODE_CHARACTER_CLASS 隐含 UNICODE_CASE | testCompileAndExceptions |
| P0 | matches 便捷 + null→NPE；matcher 分组 group/start/end/groupCount + matcher(null)→NPE | testMatchesConvenience、testMatcherGroups |
| P0 | 命名捕获组 `(?<name>...)` group(name)/start(name)/end(name)；重复命名组→PatternSyntaxException | testNamedGroups |
| P0 | 各标志行为：LITERAL/CASE_INSENSITIVE/MULTILINE/DOTALL/COMMENTS/UNIX_LINES/UNICODE_CHARACTER_CLASS | testFlagsBehavior |
| P0 | split 全部 limit 语义 + 尾随空串 + 无匹配 + null→NPE | testSplit |
| P0 | quote 字面化 + null→NPE；asPredicate/splitAsStream + null | testQuote、testPredicatesAndStream |
| P1 | 边界：a*/a+/空 pattern/内联标志/量词/词边界；锚点/反向引用/转义 | testEdgePatterns、testBoundaryEscapesGroups |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |

## 关键规范细节（易错，已锁入断言）

- **JDK-8 不校验 flags 位掩码**（`compile(regex, unknownFlag)` 不抛 IAE，与后续 JDK 不同）——故 JDK-8 侧不
  断言 unknown flag→IAE。
- `UNICODE_CHARACTER_CLASS` 在 `flags()` 中隐含返回 `UNICODE_CHARACTER_CLASS | UNICODE_CASE`。
- 坏 pattern 抛的是 `PatternSyntaxException`（`IllegalArgumentException` 子类），不是通用 IAE。
- `split` 的 limit=0 丢弃尾随空串，limit<0 保留。

## 注意事项

- 全部固定输入/正则；不依赖默认 Locale；无墙钟/随机/文件/网络；headless-safe 不加 `@key`。
