# java.util.NavigableSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。lower/floor/ceiling/higher（越界 null）、pollFirst/pollLast、
> descendingSet/descendingIterator、含 boolean 含/排的范围视图、subSet(from>to) IAE、
> 自然序 null 元素 NPE。

## 1. API 提供了哪些功能

- **最近匹配**：`lower/floor/ceiling/higher`（越界返回 null）。
- **弹出**：`pollFirst/pollLast`。
- **降序视图**：`descendingSet`、`descendingIterator`。
- **范围视图**：`subSet/headSet/tailSet` 的 SortedSet 版本 + 含 boolean 的 Navigable 版本。

## 2. Java 用户如何使用

- `navigableSet.floor(x)` 找 <=x 的最大元素；`pollFirst()` 弹最小项；
  `descendingIterator()` 反序遍历；`subSet(2,true,4,true)` 得闭区间 [2,4]。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/NavigableSet.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/TreeSet.java`。

## 4. 测试用例设计（映射到 NavigableSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 最近匹配 + null 元素 NPE | testNearest |
| P0 | pollFirst/pollLast + 空 null | testPoll |
| P0 | 降序视图 | testDescending |
| P0 | 含 boolean 范围视图 + from>to IAE | testRangeViews |

## 关键规范细节（易错，已锁入断言）

- 最近匹配越界返回 **null**（不抛异常）；`pollFirst/pollLast` 空返回 null。
- 含 boolean 的 `subSet/headSet/tailSet` 语义：`subSet(2,true,4,true)` == [2,4]。
- `subSet(from>to)` 抛 **IAE**；自然序 null 元素抛 **NPE**。

## 注意事项

- 用 TreeSet（自然序，拒 null 元素）为载体；断言顺序确定。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testNearestMatrix` — {1,3,5} 上 lower/floor/ceiling/higher **7×4=28 格全矩阵**
  （存在键/洞键/双侧越界）；降序视图上 lower/higher 镜像语义；lower/floor/ceiling/higher(null)
  → NPE（自然序）。
- `testPollExhaustion` — pollFirst 升序抽干→空后 poll 返回 null→可复用；descending 视图
  pollFirst == 原 pollLast（作用同一底层）。
- `testDescendingLiveness` — descendingSet 活视图：add/remove 透传、迭代全序倒序、
  descendingSet().descendingSet() 恢复升序；自然序集合的降序视图 comparator **非 null**
  （显式逆序比较器）；降序视图上的 tailSet/headSet 按**逆序语义**（tailSet(3)=自然序<=3、
  降序方向输出）。
- `testBooleanRangeMatrix` — subSet 四布尔组合 [..]、headSet/tailSet 含/排他双侧；
  **[3,3] 闭闭区间含 3、(3,3]/[3,3) 为空**；from>to IAE（不论旗标）；
  descendingIterator 全序列 + 其 remove 作用底层（删的是先产出的最大元素）。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| lower/floor/ceiling/higher(null)（自然序） | NullPointerException |
| subSet(from>to)（任意 inclusive 旗标） | IllegalArgumentException |
| 空 pollFirst/pollLast | 返回 null（无异常） |
