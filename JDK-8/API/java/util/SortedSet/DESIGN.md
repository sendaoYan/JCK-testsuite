# java.util.SortedSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。comparator（自然序 null / 自定义非 null）、first/last 空 NSEE、
> headSet/tailSet/subSet 范围视图（开/闭）、subSet(from>to) IAE、自然序 null 元素 NPE、
> 范围视图活视图。

## 1. API 提供了哪些功能

- **排序元数据**：`comparator`（自然序返回 null）、`first/last`。
- **范围视图**：`headSet(to)`（排他）、`tailSet(from)`（含）、`subSet(from,to)`（[from,to)）。

## 2. Java 用户如何使用

- `sortedSet.subSet(a, b)` 得到 [a,b) 活视图；`headSet(x)` 取 <x 部分；`tailSet(x)` 取 >=x 部分。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/SortedSet.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/TreeSet.java`。

## 4. 测试用例设计（映射到 SortedSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 自然序 + null 元素 NPE | testNaturalOrder |
| P0 | 空 first/last NSEE | testEmpty |
| P0 | 自定义比较器 | testCustomComparator |
| P0 | 范围视图 + from>to IAE | testRangeViews |
| P0 | 活视图 | testBackedViews |

## 关键规范细节（易错，已锁入断言）

- `headSet` 排他、`tailSet` 含、`subSet` [from,to)；`subSet(from>to)` 抛 **IAE**。
- 自然序（comparator==null）下 `add(null)` 抛 **NPE**。
- 范围视图是**活视图**：对其增删反映到底层 Set。

## 注意事项

- 用 TreeSet（自然序，拒 null 元素）为载体；断言顺序确定。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testRangeMatrix` — headSet 排他/tailSet 包含边界矩阵（最小键/越界键/全体键）；subSet [from,to)，
  **from==to 为合法空区间**，界可越出集合；视图再取视图越界 → IAE；subSet(from>to) IAE。
- `testViewMutationRules` — 视图写规则矩阵：视图 add 越界（含排他上界本身）→ IAE；
  界内 add/remove 透传底层（size 联动）；底层界内变更对视图可见；headSet.add(toElement) IAE；
  tailSet.add(已存在 from) 返回 false（from 含于视图）；**视图 clear 只清区间**，界外元素保留。
- `testIterationOrder` — 迭代/toArray 严格升序；reverseOrder 比较器降序且 first/last 互换；
  **comparator() 返回构造时同一实例（==）**；空集 first/last NSEE。
- `testSpliteratorContract` — TreeSet spliterator **ORDERED|SORTED|DISTINCT** 且 SIZED；
  SORTED 时 getComparator()==set.comparator()（自然序两 null、定制序同实例）。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| 空 first()/last() | NoSuchElementException |
| add(null)（自然序） | NullPointerException |
| subSet(from>to) / 视图 add 越出区间 | IllegalArgumentException |
| headSet 视图 add(toElement)（排他界） | IllegalArgumentException |
