# java.text.CollationKey 测试设计案（JDK-8）

> 覆盖目标：完整契约（抽象结构、getSourceString、compareTo 契约、Comparable 排序、toByteArray、
> 子类、拒绝外来实现、collator 独立性），非冒烟。

## 1. API 提供了哪些功能

- 构造：`protected CollationKey(String source)`。
- 查询：`getSourceString()`；抽象 `compareTo(CollationKey)`（Comparable）、`toByteArray()`。

## 2. Java 用户如何使用

- `Collator.getCollationKey(String)` 预计算排序键，`Collections.sort(keys)` 排序；
  `toByteArray()` 持久化跨进程比较。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/CollationKey.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 CollationKeyTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、Comparable、protected 构造器、抽象方法 | testWhiteBox |
| P0 | getSourceString 原文往返 | testBlackBox |
| P0 | compareTo 反对称/传递/自比/与 Collator 同号 | testBlackBox |
| P0 | 相等文本两次取 key 互比 0 | testBlackBox |
| P0 | Comparable 排序（alpha<bravo<charlie<delta） | testBlackBox |
| P0 | toByteArray 非空/确定性/区分异文本 | testBlackBox |
| P0 | 子类（MyKey）+ 拒绝外来实现（ClassCastException） | testBlackBox |
| P0 | 独立 collator 顺序一致 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- RuleBasedCollationKey 与外来 CollationKey 子类比较抛 `ClassCastException`。

## 注意事项

- 固定 Locale.ENGLISH（Collator.getInstance 返回 RuleBasedCollator）；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 结构（abstract、protected ctor、Comparable、非 Serializable）
- compareTo 与 Collator.compare 一致 + 反对称 + 相等源相等键 + toByteArray 字节级确定性
- Collections.sort(keys) 与 sort(strings, collator) 结果全等；PRIMARY 下键折叠大小写
