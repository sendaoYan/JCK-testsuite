# java.text.StringCharacterIterator 测试设计案（JDK-8）

> 覆盖目标：完整契约（三个构造器、双向遍历、setText、equals/hashCode/clone、DONE/空文本、
> 边界与错误路径），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`StringCharacterIterator(String)`、`(String,int pos)`、`(String,int begin,int end,int pos)`。
- 遍历：first/last/current/next/previous/setIndex/getBeginIndex/getEndIndex/getIndex。
- 其他：`setText(String)`、`equals/hashCode`（text+pos+begin+end）、`clone()`。

## 2. Java 用户如何使用

- `for (char c = it.first(); c != DONE; c = it.next())` 双向扫描；4 参构造限定子区间；setText 换文本。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/StringCharacterIterator.java`。
- JDK-27 同目录 DESIGN.md（校验与 DONE 行为一致）。

## 4. 测试用例设计（映射到 StringCharacterIteratorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、implements CharacterIterator、三构造器、方法签名 | testWhiteBox |
| P0 | String 构造 + 全遍历 | testBlackBox |
| P0 | (String,int) 构造 | testBlackBox |
| P0 | 4 参子区间构造 | testBlackBox |
| P0 | setText 换文本复位 | testBlackBox |
| P0 | clone 独立副本 + equals/hashCode | testBlackBox |
| P0 | DONE/空文本边界 | testBlackBox |
| P0 | 错误路径（越界/begin>end/setIndex/setText(null)） | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- 越界构造/`setIndex` 抛 `IllegalArgumentException`；`setText(null)` 抛 NPE。

## 注意事项

- 无 Locale/墙钟/随机；headless-safe 不加 `@key`。
