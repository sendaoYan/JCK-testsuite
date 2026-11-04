# javax.imageio.plugins.tiff.TIFFTagSet — DESIGN

## 1. API 功能
`TIFFTagSet` 表示一组 TIFF 标签，负责把 raw 标签号映射到 `TIFFTag`。方法分组：
- 构造器：`TIFFTagSet(List<TIFFTag>)`。
- 查询：`getTag(int)`、`getTag(String)`、`getTagNumbers()`、`getTagNames()`（返回不可变排序集合）。

## 2. Java 用户如何使用
用户建 `TIFFTag` 列表后 `new TIFFTagSet(list)`；`getTag("ImageWidth")` 或 `getTag(256)` 查标签；`getTagNames()/getTagNumbers()` 遍历全部标签。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/TIFFTagSet.java`：内部用 `TreeMap` 按 number 与 name 各存一份；`getTag(int)` 未知返回 null；`getTag(String)` null 抛 `IllegalArgumentException`；`getTagNumbers/getTagNames` 返回 `unmodifiableSortedSet`。
- `BaselineTIFFTagSet`、`ExifTIFFTagSet` 等子类用 `addTag`（私有/子类构造）注册标签，通过继承的 `getTag` 查询。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/tiff/` 有 tag set 查询测试。

## 4. 测试用例设计 + 自动化实现（`TIFFTagSetTest.testBlackBox`）
- 三个标签按名/号查往返，未知名/号返回 null。
- `getTagNames/getTagNumbers` 大小一致、内容包含、号码升序。
- 重复标签号 last-wins。
- 非法入参：null list、含非 TIFFTag 的 list、`getTag(null)` 均抛异常。

## 5. 深化补充（第二批，2026-11-05，断言 29 → 77）
- 视图语义实测：`getTagNumbers()/getTagNames()` 为不可修改 SortedSet 视图，add/remove/clear 全部抛 `UnsupportedOperationException`；first/last/headSet 反映升序。
- 构造器拷贝语义：构造后修改源 List 不影响集合；含 null 元素的 List → IAE "tags contains a non-TIFFTag!"；`new TIFFTagSet(null)` → IAE（实测非 NPE）。
- 空集合：合法构造、first()/last() 抛 `NoSuchElementException`；getTag(0)/负数/空名/未知名均 miss 返回 null。
- 重复 tag number：last-wins（号查询），但两个名字都保留、名字查询不坍缩；tag number 0 参与排序。
- 与 4 个出厂集合的连接测试：全部名字↔号双向解析、最小号断言（Baseline 254、Fax 326、ExifParent 34665、GeoTIFF 33550）、getInstance 单例 `==`。
