# java.util.NavigableSet — 测试设计案

## 1. API 功能
可导航的有序 Set：最近元素查询、首末元素、区间视图、逆序视图。继承 SortedSet。

- 抽象：lower/floor/ceiling/higher/pollFirst/pollLast/descendingSet/descendingIterator。

## 2. Java 用户如何使用
`NavigableSet<E> s = new TreeSet<>();` lower/floor/ceiling/higher 最近元素、first/last、
pollFirst/pollLast 摘取、descendingSet/descendingIterator 逆序、subSet/headSet/tailSet 区间。
链路：add -> 最近元素查询 -> 首末/摘取 -> 逆序/区间视图。

## 3. 开源代码实际用法调研
- OpenJDK `TreeSet`（本地 `~/git/jdk25u-dev`）实现全部抽象方法；`NavigableSet` 继承 SortedSet——
  第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/NavigableSet/`.

## 4. 测试用例设计 + 自动化实现（NavigableSetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 最近元素 | lower/floor/ceiling/higher |
| 首末 | first/last |
| 摘取 | pollFirst/pollLast 删除并返回 |
| 逆序 | descendingSet/descendingIterator |
| 区间 | subSet/headSet/tailSet 布尔变体 |

共 5 组场景、约 16 个 check（`testBlackBox()`）。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testNearestMatrix` — {1,3,5} 上 7×4=28 格 lower/floor/ceiling/higher 全矩阵；降序视图镜像；
  四查询传 null → NPE。
- `testPollExhaustion` — pollFirst 升序抽干、空集 poll null、可复用；降序 pollFirst==原 pollLast。
- `testDescendingLiveness` — descendingSet 活视图（add/remove 透传、双降恢复升序、
  自然序降序视图 comparator 非 null、逆序语义的 tail/head 子视图）。
- `testBooleanRangeMatrix` — subSet 四布尔组合、head/tail 含排双侧、[3,3] 闭闭含 3、
  (3,3]/[3,3) 空、from>to IAE；descendingIterator 序列 + remove 作用底层。
- `testSequencedDefaults`（JDK-21+）— reversed() 同元素降序迭代；removeFirst/removeLast 端点删除
  作用底层；reversed() 视图删透传、端点翻转；空集 removeFirst/removeLast NSEE。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| lower/floor/ceiling/higher(null)（自然序） | NullPointerException |
| subSet(from>to)（任意旗标） | IllegalArgumentException |
| 空 pollFirst/pollLast | null（无异常） |
| 空可变 TreeSet removeFirst/removeLast | NoSuchElementException |

### JDK-8 vs JDK-27 差异（本类）
- 27 侧 NavigableSet 新增 default removeFirst/removeLast/reversed()（JDK-21+，TreeSet 实现为
  pollFirst/pollLast 语义与降序视图）；8 侧无。
