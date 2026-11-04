# java.util.regex.MatchResult 测试方案

## 1. API 功能（按方法分组）

- 抽象查询（实现类必须提供）：`start()`、`start(int)`、`end()`、`end(int)`、`group()`、`group(int)`、`groupCount()`。
- 默认方法（可继承）：`start(String)`、`end(String)`、`group(String)`（依赖 `namedGroups()` 委托）。
- 默认方法（需覆盖才有意义）：`namedGroups()`（默认抛 UnsupportedOperationException）、`hasMatch()`（默认抛 UnsupportedOperationException）。

## 2. Java 用户如何使用

- 通常不直接 new 实现，而是从 `Matcher` 获得：`Matcher` 实现 `MatchResult`，
  `m.toMatchResult()` 返回不可变快照 `MatchResult`。
- 典型调用：
  - 读匹配结果：`mr.start()`/`mr.end()`/`mr.group()`。
  - 命名组：`mr.group("name")`、`mr.start("name")`、`mr.end("name")`。
  - 在 `replaceAll(Function<MatchResult,String>)` 回调里消费 `MatchResult`。
- 真实组合链路：
  1. `Pattern.compile(regex).matcher(input).find()` 后把 `Matcher` 当 `MatchResult` 读结果。
  2. `toMatchResult()` 生成快照，后续 `find()` 不改变快照。
  3. 自定义实现仅覆盖 `namedGroups()` 即可让三个命名默认方法工作。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.regex.MatchResult.java:30-40`：`@implNote` 明确说明 `start(String)`/`end(String)`/`group(String)`
  默认方法都依赖 `namedGroups()`，而 `namedGroups()` 默认抛 `UnsupportedOperationException`，
  "It is thus sufficient to override namedGroups() for these methods to work"。
- JDK 源码 `java.util.regex.MatchResult.java:290-330`：`namedGroups()` 默认实现 `throw new UnsupportedOperationException("namedGroups()")`；
  `groupNumber(String)` 私有方法 `Objects.requireNonNull(name)` 后 `namedGroups().get(name)`，找不到抛
  `IllegalArgumentException("No group with name <" + name + ">")`。
- JDK 源码 `java.util.regex.MatchResult.java:340-360`：`hasMatch()` 默认实现 `throw new UnsupportedOperationException("hasMatch()")`。
- JDK 测试 `test/jdk/java/util/regex/ImmutableMatchResultTest.java`：`toMatchResult()` 返回的快照
  不随 `Matcher` 后续匹配变化。
- JDK 源码 `java.util.regex.Matcher.java:1547` `replaceAll(Function)`：`Function<MatchResult,String>` 回调
  即本接口的消费场景。

## 4. 测试用例设计 + 自动化实现

实现位置：`MatchResultTest.java`（`testWhiteBox()` 白盒 + 下列黑盒方法）。

- `testWhiteBox()`：接口形态、抽象方法、五个默认方法（`isDefault()`）。
- `testDefaultNamedMethods()`：自定义实现覆盖 `namedGroups()`，验证 `start(String)/end(String)/group(String)`
  按名称→编号委托；未知名抛 IllegalArgumentException、null 名抛 NPE。
- `testBareDefaults()`：裸实现未覆盖 `namedGroups()/hasMatch()` 时抛 UnsupportedOperationException。
- `testMatcherAsMatchResult()`：`Matcher` 作为 `MatchResult` 的读写 + `toMatchResult()` 返回快照类型。
- 健壮性：全部固定输入与正则；无墙钟/随机/无序集合迭代（namedGroups 用 get，不迭代顺序）；
  无全局副作用；无文件/网络/端口；跨平台无路径/编码依赖；headless-safe 不加 `@key`。
