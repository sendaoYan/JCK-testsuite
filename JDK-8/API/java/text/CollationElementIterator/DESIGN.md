# java.text.CollationElementIterator 测试设计案（JDK-8）

> 覆盖目标：完整契约（final、无公开构造器、NULLORDER、next/previous/reset/offset/order 提取、
> setText/getMaxExpansion、正反向遍历），非冒烟。

## 1. API 提供了哪些功能

- 常量：`NULLORDER=-1`。
- 遍历：`next()`、`previous()`、`reset()`、`getOffset()`、`setOffset(int)`、
  `setText(String)`、`setText(CharacterIterator)`。
- 序提取：静态 `primaryOrder(int)`、`secondaryOrder(int)`、`tertiaryOrder(int)`；`getMaxExpansion(int)`。
- `final` 类，无公开构造器（经 `RuleBasedCollator.getCollationElementIterator` 获得）。

## 2. Java 用户如何使用

- `RuleBasedCollator.getCollationElementIterator("abc")` → `next()` 逐元素直到 `NULLORDER`；
  `previous()` 反向；`primaryOrder/secondaryOrder/tertiaryOrder` 拆解排序元素。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/CollationElementIterator.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 CollationElementIteratorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、无公开构造器、NULLORDER=-1、方法签名 | testWhiteBox |
| P0 | next/previous/reset/offset/setOffset/setText | testBlackBox |
| P0 | primaryOrder/secondaryOrder/tertiaryOrder/getMaxExpansion | testBlackBox |
| P0 | 正向全遍历 + 反向 previous 逆序 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- `NULLORDER == -1`；`previous()` 反向逆序返回元素。

## 注意事项

- 无 Locale 依赖（规则显式给出）；无墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 确定性 order 表（"abc"→0x520000/0x530000/0x540000，primary=82，NULLORDER=0xFFFFFFFF）
- offset 控制（next 前进、previous 回退、setOffset(0) 回卷、setOffset(99)→IAE）
- primary/secondary/tertiary 位分解；getMaxExpansion；CharacterIterator 源等价；null→NPE
