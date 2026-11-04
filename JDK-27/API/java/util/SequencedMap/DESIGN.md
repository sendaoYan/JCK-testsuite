# java.util.SequencedMap — 测试设计案

## 1. API 功能
有明确迭代顺序的 Map 接口（JDK 21+）。继承 Map。

- 抽象：reversed()。
- default：firstEntry/lastEntry/pollFirstEntry/pollLastEntry/putFirst/putLast。

## 2. Java 用户如何使用
`SequencedMap<K,V> m = new LinkedHashMap<>();` 首尾插入/摘取条目、reversed() 逆序视图。
链路：putFirst/putLast -> firstEntry/lastEntry -> pollFirstEntry/pollLastEntry -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `SequencedMap`（本地 `~/git/jdk25u-dev`）default 方法委托 entrySet 迭代器；
  `LinkedHashMap` 实现——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/SequencedMap/`.

## 4. 测试用例设计 + 自动化实现（SequencedMapTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 首尾插入 | putFirst/putLast 顺序 |
| 首末条目 | firstEntry/lastEntry |
| 摘取 | pollFirstEntry/pollLastEntry 删除并返回 |
| reversed | 逆序视图 |

共 4 组场景、约 10 个 check（`testBlackBox()`）。

## 契约深化（第二轮，追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | putFirst/putLast 既有键**重定位+更新值**（返回旧值、不复制） | testPutFirstLastExistingKey |
| P0 | firstEntry/lastEntry/pollFirstEntry/pollLastEntry 返回**不可变快照**（setValue → UOE，映射不变） | testEntrySnapshots |
| P0 | 空语义：first/last/poll* 全返回 **null**（与 Collection 的 NSEE 相对） | testEmptySemantics |
| P0 | reversed() 活视图：rev.put 落**原映射尾端**（put 无位置语义）、view putFirst == 原 putLast、view pollFirstEntry 删原 last | testReversedLiveView |
| P0 | TreeMap 载体：读/删可用，putFirst/putLast → UOE（序约束） | testTreeMapCarrier |
| P0 | 实现者矩阵：LinkedHashMap/TreeMap 是；HashMap/Map.of 不是 | testWhoImplements |

### 易错规范细节（实测锁定）

- **Map.put 无位置语义**：`reversed().put(k,v)` 落原映射**尾端**（≠ List.reversed().add 的首端）；`reversed().putFirst(k,v)` == 原 `putLast(k,v)`。
- firstEntry/lastEntry 是**快照**（NullableKeyValueHolder 实现）：`setValue` → **UOE** 且原映射不变；poll* 先删后返快照。
- 空映射 first/last/poll* 返回 **null 不抛异常**——与 SequencedCollection 空读抛 NSEE 的不对称是规范明确的。
- Map 接口不继承 SequencedMap；只有 LinkedHashMap/TreeMap 实现。
- TreeMap putFirst/putLast → **UOE**（序约束）。
