# java.util.SequencedSet — 测试设计案

## 1. API 功能
有明确迭代顺序且元素不重复的集合接口（JDK 21+）。继承 SequencedCollection 与 Set。

- 抽象：reversed()。

## 2. Java 用户如何使用
`SequencedSet<E> s = new LinkedHashSet<>();` 保持插入顺序、reversed() 逆序视图。
链路：add -> getFirst/getLast -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `SequencedSet`（本地 `~/git/jdk25u-dev`）仅 abstract reversed()，其余继承 default；
  `LinkedHashSet` 实现——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SequencedSet/`.

## 4. 测试用例设计 + 自动化实现（SequencedSetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 顺序 | 插入顺序保持、元素不重复 |
| reversed | 逆序视图 |
| 首末 | getFirst/getLast/addFirst |

共 3 组场景、约 8 个 check（`testBlackBox()`）。

## 契约深化（第二轮，追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | addFirst/addLast 对既有元素**重定位**（不复制不报错） | testRelocation |
| P0 | null 容忍：LinkedHashSet addFirst(null) 接受（HashSet 家族） | testNullTolerance |
| P0 | 空语义矩阵：get/getLast/removeFirst/removeLast → NSEE | testEmptySemantics |
| P0 | reversed() 活视图：rev.add 落**原集合尾端**（add 无位置语义）、view removeFirst 删原 last、view addFirst(既有) 重定位到原尾 | testReversedLiveView |
| P0 | TreeSet 载体：读/删可用，addFirst/addLast → UOE（序约束） | testTreeSetCarrier |
| P0 | 实现者矩阵：LinkedHashSet/TreeSet 是；HashSet/Set.of/unmodifiableSet 包装不是 | testWhoImplements |

### 易错规范细节（实测锁定）

- **Set.add 无位置语义**：`reversed().add(x)` 落在原集合**尾端**（≠ List 的首端）；`reversed().addFirst(x)` == 原 `addLast(x)`。
- Set 接口**不**继承 SequencedSet：HashSet/Set.of 即使有插入序也不实现；只有 LinkedHashSet/TreeSet 实现。
- LinkedHashSet 容忍 null（addFirst(null) 合法）。
- TreeSet addFirst/addLast 违反排序约束 → **UOE**（不是 ISE）。
