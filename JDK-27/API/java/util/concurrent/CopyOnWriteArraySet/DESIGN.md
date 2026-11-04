# java.util.concurrent.CopyOnWriteArraySet 测试方案

## 1. API 功能（按方法分组）
- 构造器：`CopyOnWriteArraySet()`、`(Collection)`。
- 集合操作：`add(E)`（去重）、`remove(Object)`、`contains(Object)`、`size/isEmpty/clear`。
- 批量：`addAll/containsAll/removeAll/retainAll`、`removeIf/forEach`。
- 遍历：`iterator()/spliterator()/toArray()`。
- 实现：extends `AbstractSet` + `Serializable`。内部基于 `CopyOnWriteArrayList`。

## 2. Java 用户如何使用
- 读多写少去重集合：`CopyOnWriteArraySet<T> s = new CopyOnWriteArraySet<>();`。
- 重复 `add` 返回 false（保持集合语义）。
- 迭代器是快照：创建后修改不影响迭代内容，不抛 CME。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:184-185`：`CopyOnWriteArraySet` 与 `CopyOnWriteArrayList` 同为 COW 并发集合。
- JDK 测试 `test/jdk/java/util/concurrent/CopyOnWriteArraySet/`：覆盖 set 语义、去重、equals、快照等。
- 使用范式：读多写少的去重集合（如服务端点列表）。

## 4. 测试用例设计 + 自动化实现
实现位置：`CopyOnWriteArraySetTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractSet`/implements `Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `add 去重 → contains → remove → size`。
  - `addAll` 去重、`containsAll`。
  - 迭代快照（创建后 add，迭代器见旧快照）。
  - `equals/removeAll/retainAll`。
- 健壮性：固定输入、无顺序依赖（先排序再断言）、无全局副作用、headless-safe。

## 4.1 契约深化追加（第二轮，PASS 36 → 70 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | 空态：contains false、remove false、空迭代器 next NSEE、toString "[]" | testEmptyState |
| P0 | **迭代序 = 首次出现插入序**（可固定序列全等断言）：ctor(5,3,1,3,5)→[5,3,1]、addAll→[c,a,b]、add 追加尾部 | testIterationOrderIsInsertionOrder |
| P0 | **null 容忍**：add(null)/contains(null)/第二次 false/remove(null)；addAll(null)/ctor(null) NPE | testNullTolerance |
| P0 | 快照迭代精确序：取迭代器后 add，迭代内容不变全等；iterator.remove UOE | testIteratorSnapshotExactOrder |
| P1 | 跨实现 equals/hashCode 与 HashSet 对称 | testCrossImplEqualsHashSet |
| P1 | 序列化 round-trip（含 null、保插入序、suid 锁定、复活可 add） | testSerialization |
| P1 | spliterator：DISTINCT\|IMMUTABLE\|SIZED\|SUBSIZED（实测 int=17473，只断关键位） | testSpliteratorCharacteristics |

### 易错规范细节（实测记录）

- COWAS **允许 null 元素**（只有一个，去重）——与 CHM/CSLM/CSLS null 全拒对照。
- 迭代序是**确定的插入序**（独有契约），修正了原设计"先排序再断言"的保守做法，改为固定序列全等。
- iterator.remove → UnsupportedOperationException（快照）。
- serialVersionUID = 5457747651344034263L（与 JDK-8 一致）。
