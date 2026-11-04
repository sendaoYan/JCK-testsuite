# java.text.CharacterIterator 测试设计案（JDK-8）

> 覆盖目标：完整契约（接口结构、双向游标、DONE 边界、clone、子区间、setIndex 校验），非冒烟。

## 1. API 提供了哪些功能

- 接口：`first/last/current/next/previous/setIndex/getBeginIndex/getEndIndex/getIndex/clone`。
- 常量：`DONE == '￿'`（越界返回值）；extends `Cloneable`。

## 2. Java 用户如何使用

- `for (char c = it.first(); c != CharacterIterator.DONE; c = it.next())` 经典遍历；
  next/previous/setIndex 双向编辑游标。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/CharacterIterator.java`。
- JDK-27 同目录 DESIGN.md（DONE 与边界行为一致）。

## 4. 测试用例设计（映射到 CharacterIteratorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：接口、extends Cloneable、9 个抽象方法、DONE==0xFFFF | testWhiteBox |
| P0 | first/last/current + 索引 | testBlackBox |
| P0 | next/previous + DONE 边界 | testBlackBox |
| P0 | setIndex 定位 | testBlackBox |
| P0 | getBeginIndex/getEndIndex | testBlackBox |
| P0 | clone 独立副本 | testBlackBox |
| P0 | 子区间 + 空文本 | testBlackBox |
| P0 | setIndex 越界抛 IAE | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- `DONE == (char)0xFFFF`；`next()` 越过 end、`previous()` 越过 begin 均返回 DONE。

## 注意事项

- 无 Locale/墙钟/随机；headless-safe 不加 `@key`。
