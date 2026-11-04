# java.util.SequencedCollection — 测试设计案

## 1. API 功能
具有明确迭代顺序的集合接口（JDK 21+）。继承 Collection。

- 抽象：reversed()。
- default：addFirst/addLast/getFirst/getLast/removeFirst/removeLast。

## 2. Java 用户如何使用
`SequencedCollection<E> c = new ArrayList<>();` 在首尾插入/取值/删除、reversed() 逆序视图。
链路：addFirst/addLast -> getFirst/getLast -> removeFirst/removeLast -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `SequencedCollection`（本地 `~/git/jdk25u-dev`）default 首尾方法委托迭代器/removeLast；
  `List`/`Deque` 继承并覆写——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SequencedCollection/`.

## 4. 测试用例设计 + 自动化实现（SequencedCollectionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 首尾插入 | addFirst/addLast 顺序 |
| 首尾取值 | getFirst/getLast |
| 首尾删除 | removeFirst/removeLast 删除并返回 |
| reversed | 逆序视图 |

共 4 组场景、约 10 个 check（`testBlackBox()`）。
