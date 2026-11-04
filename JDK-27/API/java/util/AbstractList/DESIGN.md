# java.util.AbstractList — 测试设计案

## 1. API 功能
`List` 的随机访问骨架：只要求实现 `get(int)` 与 `size()`，其余位置操作基于 get/size 提供。

- 抽象：`get(int)`。
- 骨架实现：`add(E)`（追加）、`add(int,E)/set(int,E)/remove(int)`（默认抛 UnsupportedOperationException，
  供子类覆写）、`indexOf/lastIndexOf/clear/addAll(int,..)/iterator/listIterator/listIterator(int)/
  subList/equals/hashCode`；`protected transient int modCount` 支撑 fail-fast。

## 2. Java 用户如何使用
自定义 List 覆写 get+size 得只读随机访问列表；要可写再覆写 set/add/remove。fail-fast 迭代器在
结构修改后抛 ConcurrentModificationException。subList 返回活视图。
链路：new 子类 -> add/get/set/remove 位置操作 -> indexOf -> iterator/listIterator -> subList
-> equals/hashCode -> 结构修改触发 CME。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/AbstractList.java`（本地 `~/git/jdk25u-dev`）：
  `set/add(int)/remove(int)` 抛 UnsupportedOperationException；iterator 检查 modCount 实现
  fail-fast——第 3/7 节断言对齐。
- OpenJDK `test/jdk/java/util/AbstractList/FailFastIterator.java`、`CheckForComodification.java`、
  `HasNextAfterException.java`：官方测试专攻 fail-fast 与迭代器异常语义。
- OpenJDK `src/java.base/share/classes/java/util/ArrayList.java` 即继承 AbstractList 的标准实现。

## 4. 测试用例设计 + 自动化实现（AbstractListTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 位置操作 | add(E) 追加、add(int,E) 插入、set 返回旧值、remove(int) 返回并删除 |
| indexOf/lastIndexOf | equals 判定、缺省 -1 |
| iterator fail-fast | 结构修改后 next() 抛 ConcurrentModificationException |
| listIterator | next/add/set/previous/remove 链路 |
| subList 视图 | 修改视图反映到底层；结构修改后视图 size 更新 |
| equals/hashCode | 等值列表相等、顺序敏感 |
| 只读骨架 | set/add(int)/remove(int) 抛 UnsupportedOperationException |
| clear | 清空 |

共 8 组场景、约 22 个 check（`testBlackBox()`）。

## 深化批次 II（契约级追加）

- **继承 iterator 走 get() 证明**：只实现 get/size 的子类，for-each 恰好驱动 size() 次 get()；
  未 next 先 remove→ISE；空表 next→NSEE（get 抛 IOOBE 被捕获转换）；forEachRemaining。
- **add(E) 分派证明**：只实现 add(int,E) 的子类上 `add(e)` 返回 true 且恰好调用一次 add(int,E)；
  addAll 逐元素分派（计数器断言）。
- **iterator.remove 分派**：未实现 remove(int) 的子类上迭代器 remove 落到 UOE；实现了则透传删除。
- **hashCode 公式**（4066/126046）并与其他实现一致。
- **subList 写通道**：add/remove(Object)/remove(int)/set/iterator.remove 全透传；空段视图；越界 IOOBE/IAE。
- **JDK-27 追加**：SequencedCollection 默认在最小子类上的分派——getFirst/getLast 走 get 可用、
  addFirst 落到未实现的 add(int,E)→UOE、空表 getFirst/getLast→NSEE、reversed() 视图。
