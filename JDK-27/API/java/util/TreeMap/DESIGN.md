# java.util.TreeMap 测试设计案（JDK-27）

> 覆盖目标：**完整契约**，白盒（反射结构）+ 黑盒（行为契约）。构造器矩阵、null 歧义、NavigableMap
> 边界、范围视图活对象写通道、fail-fast、默认方法深分支、toArray 契约、跨实现 equals、clone/序列化、
> 状态不变量全部补齐；外加 JDK 21+ SequencedMap 特有 `putFirst/putLast/reversed`（有序 Map 语义）。

## 1. API 提供了哪些功能

- **构造器（4）**：`TreeMap()`、`TreeMap(Comparator)`、`TreeMap(Map)`、`TreeMap(SortedMap)`。
- **核心操作**：`put`、`get`、`remove`、`putAll`、`clear`、`containsKey`、`containsValue`、`size`。
- **NavigableMap 查询**：`firstKey/lastKey`、`firstEntry/lastEntry`、`lower/loor/ceiling/higher` 的
  key+entry 变体、`pollFirstEntry/pollLastEntry`。
- **范围视图**：`subMap/headMap/tailMap`（含 boolean 含边界重载）、`descendingMap`、
  `descendingKeySet`、`navigableKeySet`；`keySet/values/entrySet`。
- **Java 8 默认方法**：`getOrDefault`、`putIfAbsent`、`replace`、`replace(K,old,new)`、`replaceAll`、
  `computeIfAbsent`、`computeIfPresent`、`compute`、`merge`、`forEach`。
- **JDK 21+ SequencedMap**：`putFirst/putLast`（有序 Map 对 TreeMap 恒抛 UOE）、`reversed()`。
- **契约继承**：`clone()`（浅拷贝）、`Serializable`（`serialVersionUID == 919286545866124006L`）。

## 2. Java 用户如何使用

- 与 JDK-8 一致：自然序/Comparator 建表、NavigableMap 上下界查询、范围视图/倒序视图直接改、
  null key 抛 NPE、null value 合法、默认方法批量计算。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** `java/util/TreeMap.html`（JDK-8 与 JDK-27）：NavigableMap/SequencedMap 契约。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/TreeMap` + `~/git/jdk/test/jdk/java/util/TreeMap`。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/TreeMap`。

## 4. 测试用例设计（映射到 TreeMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒结构：继承 AbstractMap、实现 NavigableMap/Cloneable/Serializable、成员形态 | testWhiteBox |
| P0 | 构造器矩阵 + null 语义（同 JDK-8 版） | testConstructorMatrix、testNullSemantics |
| P0 | NavigableMap 边界 + 范围视图写通道 + 倒序视图联动 | testNavigableBoundaries、testRangeViewWriteThrough、testDescendingNavigableViews |
| P0 | 视图写通道 + Entry.setValue + fail-fast | testViewWriteChannels、testEntrySetValue、testFailFast |
| P0 | 默认方法深分支 + forEach 升序 | testDefaultMethods、testForEachOrdering |
| P0 | 跨实现 equals/hashCode | testCrossImplEquals |
| P1 | toArray 契约、clone、序列化、状态不变量 | testToArrayContract、testClone、testSerialization、testStateInvariants |
| P1 | JDK 21+ SequencedMap：putFirst/putLast → UOE（有序 Map 不允许） | testBlackBox（已含） |

## 关键规范细节（易错，已锁入断言）

- 同 JDK-8 版；额外：JDK 21+ `putFirst/putLast` 对 TreeMap 恒抛 `UnsupportedOperationException`
  （有序 Map 由排序决定位置，拒绝显式首尾插入）。

## 注意事项

- 白盒反射只断言公开结构，不依赖内部实现字段。
- 序列化 serialVersionUID 与 JDK-8 相同，锁定官方稳定值。


## 5. 契约级深化追加（2026-11，186→339 断言）

新增方法：与 JDK-8 侧同源八块（comparator 序列化/迭代器 remove 模式/subMap 视图 CME 与界值/
present-null-value 分支/四方法矩阵/inclusive 界点/values 序与降序镜像/null 容忍 comparator）
另加 testSequencedTreeMap（JDK-21+）。

| 维度 | 锁定内容 |
|---|---|
| 八块同源 | 见 JDK-8 侧第 5 节（两侧行为一致，27 侧 PASS 339 断言覆盖） |
| SequencedMap on TreeMap | putFirst/putLast 抛 **UOE**（排序图不支持位置插入）且失败后不变；reversed()==descendingMap()（equals 相等）活视图：put/remove/poll 双向传导；reversed().descendingMap() equals 原图 |
