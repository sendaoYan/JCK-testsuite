# java.util.AbstractSequentialList — 测试设计案

## 1. API 功能
`List` 的顺序访问骨架：只要求实现 `listIterator(int)`（+size），位置操作 get/set/add/remove
都通过 listIterator 完成。

- 抽象：`listIterator(int)`。
- 骨架：`get(i)=listIterator(i).next()`、`set(i,e)=listIterator(i).set(e)`、
  `add(i,e)=listIterator(i).add(e)`、`remove(i)=listIterator(i).remove()`、`addAll(int,..)`、
  `iterator()`=listIterator()。

## 2. Java 用户如何使用
顺序数据结构（链表类）只写 listIterator 即获得完整 List 位置操作。
链路：new 子类 -> get/set/add(int)/remove(int) -> addAll(int) -> iterator/listIterator ->
indexOf/subList。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/AbstractSequentialList.java`（本地
  `~/git/jdk25u-dev`）：`get` 返回 `listIterator(index).next()`、`set` 调 `listIterator(index).set`、
  `remove` 调 `listIterator(index).remove`——第 1/2 节对齐。
- OpenJDK `test/jdk/java/util/AbstractSequentialList/AddAll.java`：官方测试专攻 addAll。
- OpenJDK `src/java.base/share/classes/java/util/LinkedList.java` 继承 AbstractSequentialList。

## 4. 测试用例设计 + 自动化实现（AbstractSequentialListTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| get/set | get 按索引、set 返回旧值并更新 |
| add/remove | add(int) 插入、remove(int) 返回并删除 |
| addAll(int) | 按序插入 |
| iterator/listIterator | for-each 遍历、双向 next/previous、nextIndex/previousIndex |
| 继承方法 | indexOf/lastIndexOf/subList |

共 5 组场景、约 13 个 check（`testBlackBox()`）。
