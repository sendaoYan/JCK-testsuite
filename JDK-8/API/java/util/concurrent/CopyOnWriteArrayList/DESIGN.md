# java.util.concurrent.CopyOnWriteArrayList 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（3）**：`()`、`(Collection)`、`(E[])`（**防御性拷贝**，改原数组不影响）。
- **List 全操作**：add/get/set（返回旧值）/remove 两种/indexOf/lastIndexOf/add(i,e)/addAll(i,c)/subList/sort/replaceAll。
- **COW 特有**：`addIfAbsent(E)`、`addAllAbsent(Collection)`（返回新增个数）。
- **快照语义**：`iterator()`/`listIterator(i)` 遍历创建瞬间的数组快照，修改原 list 不影响迭代；
  迭代器自身**不支持** remove/set/add（UOE）。
- 契约：`implements List, RandomAccess, Cloneable, Serializable`；**允许 null 元素**（与 CHM/CSLM 对照）。

## 2. 使用方式 + 开源用法调研

- 读多写少场景（监听器列表）：`CopyOnWriteArrayList<Listener> ls; for (l : ls) l.fire();` 迭代期间
  别的线程增删监听器完全安全且不抛 CME。
- JDK 源码中 `java.util.concurrent` 多处以 COWAL 保存监听/回调列表。
- OpenJDK 回归 `test/jdk/java/util/concurrent/CopyOnWriteArrayList/`：覆盖 indexOf(null)、
  迭代器不抛 CME、addAllAbsent、subList。
- 使用范式：快照迭代 + 每次写复制底层数组（写代价 O(n)，读无锁）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`CopyOnWriteArrayList`——"snapshot style iterator"）。
- OpenJDK `test/jdk/java/util/concurrent/CopyOnWriteArrayList/`。
- 双侧实测（8u372-b07 / 27.0.1）。

## 4. 测试用例设计（CopyOnWriteArrayListTest.java，PASS 115 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：List/RandomAccess/Cloneable/Serializable + addIfAbsent/addAllAbsent/clone 签名 | testWhiteBox |
| P0 | add/get/set/remove/indexOf 基本链（保留原有 testList） | testList、testListSemantics |
| P0 | addIfAbsent true/false + addAllAbsent 计数 | testAddIfAbsent |
| P0 | **null 元素全支持**：add(null)/indexOf(null)/lastIndexOf(null)/contains/set/remove(null)/addIfAbsent(null) | testNullElements |
| P0 | **快照契约**：取迭代器后 add+set，next 输出旧内容全等 | testIteratorSnapshot |
| P0 | **快照 vs ArrayList CME 对照**（同场景 ArrayList 抛 CME、COWAL 不抛且内容不变）；listIterator 快照 | testIteratorSnapshotVsArrayList |
| P0 | 迭代器 remove/set/add → UOE；listIterator(越界) IOOBE | testIteratorMutatorsUnsupported |
| P0 | 索引边界：get(-1)/get(2)/add(99,·)/remove(99)/set(5,·) IOOBE；ctor/addAll null NPE | testIndexBounds |
| P0 | ctor(E[]) 防御拷贝独立 + ctor(Collection) 保序 | testConstructors |
| P0 | subList set/add/remove 写通道直通父 + 越界 IOOBE | testSubList |
| P1 | sort 排序 + 稳定性；replaceAll | testSortReplaceAll、testSortStability |
| P1 | 跨实现 equals/hashCode/toString 与 ArrayList 全等 | testEquals |
| P1 | toArray 快照独立（改 list 不影响数组）；removeAll/retainAll/removeIf/forEach | testBulkAndToArray |
| P1 | spliterator：ORDERED|IMMUTABLE|SIZED、estimateSize 精确、tryAdvance、stream | testSpliteratorAndStream |
| P1 | 序列化 round-trip（含 null 元素、suid 锁定、复活可变） | testSerialization |
| P1 | clone 浅拷贝结构独立 | testClone |

### 易错规范细节（实测记录）

- COWAL **允许 null**：add(null)/indexOf(null)/contains(null)/set(i,null)/addIfAbsent(null) 全部合法 —— 与 CHM/CSLM 的 null 全拒形成对照记忆点。
- 迭代器三变异操作全 UOE（快照迭代器无写通道）；这是"迭代器活对象"清单里 COW 的**反向**契约。
- 越界异常类型（8 与 27 消息不同、类型一致）：`get(-1)`/`remove(99)` → `ArrayIndexOutOfBoundsException`（IOOBE 子类）；`add(99,x)`/`set(5,x)`/`listIterator(4)`/`subList` → `IndexOutOfBoundsException` —— 只断言类型不断言消息。
- `toArray` 是弱快照（数组独立于后续修改）；`ctor(E[])` 有防御拷贝。
- sort 稳定（相等键保序，List 契约）。
- serialVersionUID = 8673264195747942595L（双侧一致）。
