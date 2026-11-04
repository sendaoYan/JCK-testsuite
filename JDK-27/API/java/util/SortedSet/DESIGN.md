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

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testRangeMatrix` — 边界矩阵（同 8 侧）：headSet 排他/tailSet 包含、from==to 合法空区间、
  界外键、视图再取视图越界 IAE、subSet(from>to) IAE。
- `testViewMutationRules` — 视图写规则矩阵：越界 add IAE、透传/size 联动、
  tailSet.add(已存在 from) false、视图 clear 只清区间。
- `testIterationOrder` — 升序迭代/toArray、reverseOrder 降序、comparator() 同实例（==）、
  空集 first/last NSEE。
- `testSpliteratorContract` — ORDERED|SORTED|DISTINCT + SIZED；getComparator()==set.comparator()。
- `testSequencedDefaults`（JDK-21+）— getFirst/getLast==first/last；removeFirst/removeLast 删除并返回
  端点且作用底层；**addFirst/addLast → UOE（会破坏排序序）**；reversed() 为活视图（父增删可见、
  视图删透传）、reversed().reversed() 恢复升序；空集 getFirst/getLast/removeFirst/removeLast → NSEE。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| 空 first()/last()/getFirst()/getLast() | NoSuchElementException |
| 空 removeFirst()/removeLast()（TreeSet 可变） | NoSuchElementException |
| addFirst/addLast（TreeSet） | UnsupportedOperationException |
| add(null)（自然序） | NullPointerException |
| subSet(from>to) / 视图 add 越界 | IllegalArgumentException |

### JDK-8 vs JDK-27 差异（本类）
- 27 侧 SortedSet extends **SequencedSet**（JDK-21+）：新增 default addFirst/addLast（TreeSet 上 UOE）、
  getFirst/getLast/removeFirst/removeLast、reversed()；8 侧无这些方法。
