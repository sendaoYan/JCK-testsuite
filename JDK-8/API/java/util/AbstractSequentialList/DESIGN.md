# java.util.AbstractSequentialList 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。`get/set/add/remove` 委托给 `listIterator`、`add(E)` 经 `add(size,e)` 追加、
> `indexOf/lastIndexOf`（equals 语义，含 null）、`iterator` 顺序、`subList` live 视图、越界 IOBE。

## 1. API 提供了哪些功能

- **抽象方法**：`listIterator(int)`、`size()`。
- **默认实现（全部委托 listIterator）**：`get(int)`、`set(int,E)`、`add(int,E)`、`remove(int)`、
  `add(E)`（委托 `add(size, e)`）、`iterator()`（委托 `listIterator()`）。

## 2. Java 用户如何使用

- 基于链表的自定义 List 只需实现 `listIterator(int)` + `size()`；其余按位置操作自动委托给迭代器。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/AbstractSequentialList.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/AbstractSequentialList.java`
  （`get` 用 `listIterator(index).next()`；`add(E)` 用 `add(size(), e)`；`remove` 用 `listIterator(index).remove()`）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/AbstractSequentialList`。

## 4. 测试用例设计（映射到 AbstractSequentialListTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | get/set/add(int)/remove(int)/add(E) 委托 | testDelegation |
| P0 | indexOf/lastIndexOf + iterator 顺序 | testIndexOfIterator |
| P0 | subList live 视图 | testSubList |
| P0 | 越界 IOBE | testDefaultOpsUOE |

## 关键规范细节（易错，已锁入断言）

- `add(E)` 经 `add(size(), e)` 追加到末尾（LinkedList 语义）。
- `get/set/add(int)/remove(int)` 全部委托 `listIterator(int)`；越界由底层 LinkedList 抛 IOBE。
- `indexOf/lastIndexOf` 用 `equals`（null 用 `==`）。
- `subList` 是 live 视图（双向联动）。

## 注意事项

- 无 GC/图形/网络/文件依赖；不依赖迭代顺序（按索引断言）。

## 深化批次 II（契约级追加）

- **全操作经 listIterator 计数器证明**：get/set/add(int,E)/remove(int)/indexOf/lastIndexOf/iterator()/
  add(E)/clear 各驱动恰好一次 listIterator 创建（equals 也走一次——计数矩阵含此）。
- **只读子类 UOE**：listIterator 不可变（unmodifiableList / List.of）时 set/add/remove/add(E)/clear 全 UOE，
  读操作正常。
- **hashCode 公式**（4066/126046，继承 AbstractList equals/hashCode）。
- **subList 写通道**与越界（IOOBE/IAE）；空表 get(0)/remove(0) IOOBE；listIterator(±) IOOBE。
- **JDK-27 追加**：SequencedCollection 默认方法经 listIterator/add(int,E) 路由（getFirst/getLast/addFirst/
  addLast/removeFirst/removeLast/reversed）。
