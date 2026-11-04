# java.text.BreakIterator 测试设计案（JDK-8）

> 覆盖目标：完整契约（抽象层次、DONE、四种实例、字/词/句/行边界、导航、getText/clone、
> 受保护构造器子类），非冒烟。

## 1. API 提供了哪些功能

- 常量：`DONE=-1`。
- 工厂：`getWordInstance/getLineInstance/getCharacterInstance/getSentenceInstance`（含 Locale 重载）、
  `getAvailableLocales()`。
- 遍历：`first/last/next/next(int)/previous/following(int)/preceding(int)/current/isBoundary(int)`。
- 文本：`getText()`、`setText(String)`、抽象 `setText(CharacterIterator)`。
- 契约：`clone()`；`implements Cloneable`；受保护构造器。

## 2. Java 用户如何使用

- `BreakIterator.getWordInstance(Locale.US)` → `setText(s)` → `first()/next()` 循环直到 DONE
  定位边界。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/BreakIterator.java`。
- JDK-27 同目录 DESIGN.md（行为一致；JDK-8 字符迭代器对组合重音同样按字素簇 `{0,2}`）。

## 4. 测试用例设计（映射到 BreakIteratorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、Cloneable、DONE=-1、四种工厂非空 | testWhiteBox |
| P0 | 字符边界（含组合重音字素簇） | testCharacterBoundaries |
| P0 | 词/句/行边界 | testWordBoundaries/testSentenceBoundaries/testLineBoundaries |
| P0 | 导航（next(int)/following/preceding/isBoundary/current） | testNavigation |
| P0 | getText + clone | testTextAndClone |
| P0 | 受保护构造器子类（MinimalBreakIterator） | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- 字符实例对 `"á"` 返回 `{0,2}`（组合重音为单一字素簇）。
- `next()` 越过末尾返回 `DONE(-1)`。

## 注意事项

- 固定 Locale.US；无墙钟/随机；headless-safe 不加 `@key`。
