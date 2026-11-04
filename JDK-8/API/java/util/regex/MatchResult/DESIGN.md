# java.util.regex.MatchResult 测试设计案（JDK-8）

> 覆盖目标：接口形态 + 7 个抽象方法行为（经 Matcher、自定义实现、toMatchResult 快照），非冒烟。

## 1. API 提供了哪些功能

- 抽象方法（实现类必须提供）：`start()`、`start(int)`、`end()`、`end(int)`、`group()`、`group(int)`、
  `groupCount()`。
- JDK-8 的 MatchResult **仅有这 7 个抽象方法，无任何默认方法**（`group(String)`/`start(String)`/
  `end(String)`/`namedGroups()`/`hasMatch()` 均为后续 JDK 加入，JDK-8 不存在）。

## 2. Java 用户如何使用

- 通常不直接实现，而是从 `Matcher` 获得：`Matcher` 实现 `MatchResult`，`m.toMatchResult()` 返回不可变快照。
- 典型调用：读 `mr.start()`/`mr.end()`/`mr.group()`/`mr.group(int)`/`mr.groupCount()`。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc（`java/util/regex/MatchResult.html`）。
- JDK-27 同目录 DESIGN.md（JDK-27 的默认方法/命名组部分不适用于 JDK-8，已剔除）。

## 4. 测试用例设计（映射到 MatchResultTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：接口；7 个抽象方法；恰好 7 个抽象方法（无 default） | testWhiteBox |
| P0 | 自定义实现 start/end/group/groupCount 组合行为 + 缺席组 -1/null | testCustomImplementation |
| P0 | Matcher 作为 MatchResult 的读写 + toMatchResult 不可变快照 | testMatcherAsMatchResult |

## 关键规范细节（易错，已锁入断言）

- `group(int)` 对未参与组返回 null；`start(int)`/`end(int)` 对未参与组返回 -1。
- `toMatchResult()` 返回独立快照。

## 注意事项

- 固定输入；无默认 Locale/墙钟/随机；headless-safe 不加 `@key`。
