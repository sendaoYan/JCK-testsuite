# java.util.NavigableSet — 测试设计案

## 1. API 功能
可导航的有序 Set：最近元素查询、首末元素、区间视图、逆序视图。继承 SortedSet。

- 抽象：lower/floor/ceiling/higher/pollFirst/pollLast/descendingSet/descendingIterator。

## 2. Java 用户如何使用
`NavigableSet<E> s = new TreeSet<>();` lower/floor/ceiling/higher 最近元素、first/last、
pollFirst/pollLast 摘取、descendingSet/descendingIterator 逆序、subSet/headSet/tailSet 区间。
链路：add -> 最近元素查询 -> 首末/摘取 -> 逆序/区间视图。

## 3. 开源代码实际用法调研
- OpenJDK `TreeSet`（本地 `~/git/jdk25u-dev`）实现全部抽象方法；`NavigableSet` 继承 SortedSet——
  第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/NavigableSet/`.

## 4. 测试用例设计 + 自动化实现（NavigableSetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 最近元素 | lower/floor/ceiling/higher |
| 首末 | first/last |
| 摘取 | pollFirst/pollLast 删除并返回 |
| 逆序 | descendingSet/descendingIterator |
| 区间 | subSet/headSet/tailSet 布尔变体 |

共 5 组场景、约 16 个 check（`testBlackBox()`）。
