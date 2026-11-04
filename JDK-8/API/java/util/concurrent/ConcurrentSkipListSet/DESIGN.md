# java.util.concurrent.ConcurrentSkipListSet 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（3）**：`()`、`(Comparator)`、`(Collection)`；另有拷贝 `ctor(SortedSet)`（保序保比较器）。
- **有序导航**：first/last/pollFirst/pollLast；ceiling/floor/higher/lower。
- **NavigableSet 视图**：headSet/tailSet/subSet（两参与四参布尔边界）、descendingSet/descendingIterator；
  视图双向联动，越界 add → `IllegalArgumentException("key out of range")`。
- **Collection 通道**：add（重复 false）/contains/remove/containsAll/addAll（去重）/removeAll/retainAll/
  removeIf/clear/toArray/size/iterator（弱一致 + **支持 remove**）/spliterator/stream。
- 契约：`extends AbstractSet` + `implements NavigableSet, Cloneable, Serializable`；**元素全拒 null**（NPE）。

## 2. 使用方式 + 开源用法调研

- 可伸缩有序并发去重集合（底层 CSLM keySet）：并发有序遍历、范围维护、优先去重表。
- JDK javadoc 示例：`new ConcurrentSkipListSet<String>(String.CASE_INSENSITIVE_ORDER)` 大小写不敏感集。
- OpenJDK 回归 `test/jdk/java/util/concurrent/ConcurrentSkipListSet/`：导航矩阵、视图 IAE、null 拒绝。
- 使用范式：headSet/tailSet 做滑动窗口，descendingSet 逆序消费。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`ConcurrentSkipListSet`）。
- OpenJDK `test/jdk/java/util/concurrent/ConcurrentSkipListSet/`。
- 双侧实测（8u372-b07 / 27.0.1）。

## 4. 测试用例设计（ConcurrentSkipListSetTest.java，PASS 120 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：契约/构造器/导航/视图/descending 签名 | testWhiteBox |
| P0 | add 去重/first/last/contains/pollFirst 基本链（保留原有） | testSet |
| P0 | 空态：poll null、first/last NSEE、空迭代器 NSEE、toString "[]" | testEmptyState |
| P0 | **导航矩阵**（键集 10/20/30/40）：ceiling/floor/higher/lower 全分支含边界 null | testNavigationMatrix |
| P0 | **范围视图**：subSet 两/四参、head/tail 两形式布尔边界；视图活（父 add 可见）；视图写直通；**越界 add IAE（"key out of range"）** | testSubSetsFull |
| P0 | descendingSet 逆序 + 删除直通 + 二次 descending 复原；descendingIterator 全序 | testDescending |
| P0 | 迭代器 remove 支持 + 未 next ISE + 弱一致（并发 add 不抛 CME）+ 升序全等 | testIteratorContract |
| P0 | **null 全拒**：add/contains/remove/ceiling/floor/higher/lower/addAll/ctor 全 NPE | testNullRejection |
| P1 | 比较器矩阵 + ctor(Collection) 排序去重 + ctor(SortedSet) 保序保比较器 + addAll 计数 + clone 独立 | testComparatorAndConstructors |
| P1 | 批量 + 跨实现 equals/hashCode 与 TreeSet + toArray 升序/null 终止符 | testBulkAndEquals |
| P1 | spliterator：ORDERED\|SORTED\|NONNULL\|CONCURRENT、非 SIZED、tryAdvance 首元素升序 | testSpliterator |
| P1 | 序列化：自然序 + 逆序 Comparator round-trip 保序保比较器、suid 锁定 | testSerialization |

### 易错规范细节（实测记录）

- null 全拒**含导航查询**：`ceiling(null)`/`floor(null)`/`higher(null)`/`lower(null)` 都抛 NPE。
- 越界视图 add 的 IAE 消息两侧均为 **"key out of range"**（可断言消息片段）。
- equals 是 Set 语义（size+containsAll），**与 TreeSet 跨实现相等与迭代顺序无关**。
- 迭代器**支持 remove**（与 COWAL 快照迭代器的 UOE 对照）；未 next / 连续 remove → `IllegalStateException`。
- spliterator 实测特征（双侧一致 int=4373）：DISTINCT|SORTED|ORDERED|NONNULL|CONCURRENT，非 SIZED —— 测试断言关键位而非整值。
- `headSet(bound, true)` 含界视图可 add 已存在的 bound 元素（返回 false）。
- serialVersionUID = -2479143111061671589L（双侧一致）。
