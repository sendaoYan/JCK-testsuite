# java.util.SequencedSet — 测试设计案

## 1. API 功能
有明确迭代顺序且元素不重复的集合接口（JDK 21+）。继承 SequencedCollection 与 Set。

- 抽象：reversed()。

## 2. Java 用户如何使用
`SequencedSet<E> s = new LinkedHashSet<>();` 保持插入顺序、reversed() 逆序视图。
链路：add -> getFirst/getLast -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `SequencedSet`（本地 `~/git/jdk25u-dev`）仅 abstract reversed()，其余继承 default；
  `LinkedHashSet` 实现——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SequencedSet/`.

## 4. 测试用例设计 + 自动化实现（SequencedSetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 顺序 | 插入顺序保持、元素不重复 |
| reversed | 逆序视图 |
| 首末 | getFirst/getLast/addFirst |

共 3 组场景、约 8 个 check（`testBlackBox()`）。
