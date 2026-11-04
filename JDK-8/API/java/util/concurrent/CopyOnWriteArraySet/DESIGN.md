# java.util.concurrent.CopyOnWriteArraySet 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`()`、`(Collection)`（去重保序）。
- **Set 语义**：add（底层 `addIfAbsent`，重复 false）、contains/remove/containsAll/addAll（去重保序）/
  removeAll/retainAll/removeIf/clear/size/isEmpty。
- **快照语义**：iterator 遍历创建瞬间的数组，后续增删不影响；迭代器 remove → UOE。
- **迭代顺序 = 首次出现插入序**（backing COWAL 的 append 语义）。
- **允许 null 元素**（只有一个 null，去重）。
- 契约：`extends AbstractSet` + `implements Serializable`。

## 2. 使用方式 + 开源用法调研

- 读多写少的小集合去重（如已注册回调集合），迭代期间并发修改安全。
- JDK javadoc：批量操作（containsAll/addAll/removeAll/retainAll）在 size 与参数集合规模差异大时
  有 O(n) vs O(n·m) 的算法选择 —— 对用户不可见，只断言结果。
- OpenJDK 回归 `test/jdk/java/util/concurrent/CopyOnWriteArrayList/`（Set 用 COWAL 回归覆盖）。
- 使用范式：`CopyOnWriteArraySet<Listener> s;` 遍历期间增删监听器不抛 CME。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`CopyOnWriteArraySet`）。
- OpenJDK `test/jdk/java/util/concurrent/CopyOnWriteArrayList/`（共用实现）。
- 双侧实测（8u372-b07 / 27.0.1）。

## 4. 测试用例设计（CopyOnWriteArraySetTest.java，PASS 63 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends AbstractSet、构造器、add/remove/contains/批量/iterator | testWhiteBox |
| P0 | add 去重 true/false + contains/remove/size（保留原有） | testCopyOnWriteArraySet |
| P0 | 空态：contains false、remove false、空迭代器 next NSEE、toString "[]" | testEmptyState |
| P0 | **迭代序 = 首次出现插入序**：ctor(5,3,1,3,5)→[5,3,1]、addAll→[c,a,b]、add 追加尾部 | testIterationOrderIsInsertionOrder |
| P0 | **null 容忍**：add(null)/contains(null)/第二次 false/remove(null)；addAll(null)/ctor(null) NPE | testNullTolerance |
| P0 | 快照迭代：取迭代器后 add，看不到新元素；iterator.remove UOE | testIteratorSnapshot |
| P1 | removeAll/retainAll/removeIf 批量 + 跨实现 equals/hashCode 与 HashSet | testBulkOperationsAndEquals |
| P1 | toArray（保序/大数组 null 终止符）、spliterator（DISTINCT|IMMUTABLE|SIZED、trySplit、tryAdvance 首元素保序）、stream | testToArrayAndSpliterator |
| P1 | 序列化 round-trip（含 null、保插入序、suid 锁定、复活可 add） | testSerialization |

### 易错规范细节（实测记录）

- COWAS **允许 null 元素**（与 CHM/CSLM/CSLS null 全拒对照；也不同于 javadoc 里"多数并发集合拒绝 null"的说法——实测两侧均接受）。
- 迭代序**是确定的**（插入序），这是 COWAS 相对 HashSet 的独有契约，可以固定序列断言全等。
- spliterator 特征：DISTINCT|IMMUTABLE|SIZED|SUBSIZED（实测 int=17473，两侧一致；测试只断言关键位）。
- iterator.remove → UnsupportedOperationException（快照）。
- serialVersionUID = 5457747651344034263L（双侧一致）。
