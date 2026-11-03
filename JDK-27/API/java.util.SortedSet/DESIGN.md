# java.util.SortedSet — 测试设计案

## 1. API 功能
有序不重复集合基接口。继承 Set 与 SequencedSet。

- 抽象：comparator/subSet/headSet/tailSet/first/last。
- default：spliterator、addFirst/addLast（UOE）、getFirst/getLast、removeFirst/removeLast（UOE）、reversed()。

## 2. Java 用户如何使用
`SortedSet<E> s = new TreeSet<>();` 区间子集、first/last、视图，遍历保持排序。
链路：add -> first/last -> subSet/headSet/tailSet -> getFirst/getLast。

## 3. 开源代码实际用法调研
- OpenJDK `TreeSet`（本地 `~/git/jdk25u-dev`）实现全部抽象方法；addFirst/removeFirst default 抛 UOE、
  getFirst/getLast 返回 first()/last()——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SortedSet/`.

## 4. 测试用例设计 + 自动化实现（SortedSetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| comparator | 自然序 null、反向 comparator |
| 区间 | subSet/headSet/tailSet |
| 首末 | first/last/getFirst/getLast |
| addFirst | 抛 UOE（default） |
| reversed | 逆序视图 |
| spliterator | 可用 |

共 6 组场景、约 10 个 check（`testBlackBox()`）。
