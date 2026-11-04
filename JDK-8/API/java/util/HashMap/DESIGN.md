# java.util.HashMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「方法被调用一次」的冒烟。四类历史盲区——异常/参数校验、
> null 歧义语义、视图/迭代器是活对象、边界交叉场景——全部补齐。断言不依赖迭代顺序，headless 安全。

## 1. API 提供了哪些功能

- **构造器（4）**：`HashMap()`、`HashMap(int initialCapacity)`、`HashMap(int,float loadFactor)`、`HashMap(Map)`。
- **核心操作**：`put`、`get`、`remove(Object)`、`remove(Object,Object)`、`putAll`、`clear`。
- **查询**：`size`、`isEmpty`、`containsKey`、`containsValue`。
- **视图（3 个写通道）**：`keySet()`、`values()`、`entrySet()`。
- **Java 8 默认方法（10）**：`getOrDefault`、`putIfAbsent`、`replace(K,V)`、`replace(K,old,new)`、
  `replaceAll`、`computeIfAbsent`、`computeIfPresent`、`compute`、`merge`、`forEach`。
- **继承/契约**：`equals`、`hashCode`、`toString`（AbstractMap）、`clone()`、`Serializable`
  （`serialVersionUID == 362498820763181265L`）。

## 2. Java 用户如何使用

- `new HashMap<>()` 直接建表 → `put` 填充 → `get`/`containsKey` 查询 → 默认方法批量更新 → 视图遍历/删除 → `clear`。
- 用 `new HashMap<>(n)` / `new HashMap<>(n, loadFactor)` 预分配；用 `new HashMap<>(otherMap)` 拷贝。
- 把 `keySet()/values()/entrySet()` 当 Set/Collection/Set<Entry> 直接操作（删、遍历、toArray、stream）。
- 依赖 null key / null value 合法、`get`/`remove` 对缺失 key 返回 null（歧义：需 `containsKey` 区分）。
- 依赖 fail-fast：迭代中结构性修改抛 `ConcurrentModificationException`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/util/HashMap.html`）：构造器/默认方法异常契约、clone 浅拷贝、视图写通道语义。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/HashMap`（9 文件）+ `~/git/jdk/test/jdk/java/util/HashMap`（11 文件）：
  `KeySetRemove`(4286765)、`NullKeyAtResize`(7173432)、`OverrideIsEmpty`(8019381)、`ReplaceExisting`(8025173)、
  `SetValue`(4627516)、`ToString`(4189821)、`ToArray`(8336669)、`PutNullKey`(8046085)、`HashMapCloneLeak`(7042126)、
  `Bug8186171Test`(8186171)。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/HashMap`（26 文件 + `serial/`）：
  边界值表驱动（`ConstrTests` 的 invalidCapacity/negInvalidLoad）、等价类矩阵（`ContainsKeyValueTests`）、
  视图伞形套件（`EntrySetTests`/`KeySetTests`/`ValuesTests`、Iterator/Spliterator/Stream 通用套件）。

## 4. 测试用例设计（映射到 HashMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：负容量/MIN_VALUE→IAE、容量 0 合法、lf 0/±0.0/NaN/负/−∞→IAE、`HashMap(null)`→NPE、拷贝+引用一致 | testConstructors、testConstructorValidation、testCopyConstructorAndPutAll |
| P0 | putAll 四组合 + `putAll(null)`→NPE；replaceAll 原地改写+置 null | testPutAllReplaceAll、testReplaceAll |
| P0 | clone 浅拷贝 + 结构独立；toString/entry.toString null 安全 | testCloneToString、testClone |
| P0 | 三视图 × 五删除通道（iterator.remove/remove/removeAll/retainAll/clear）联动；add/addAll→UOE；`put(k,null)` 后 `keySet().remove(k)` 返回 true | testViewOperations、testViewWriteChannels |
| P0 | Entry.setValue 返回旧值并写回；setValue(null) | testEntrySetValue |
| P0 | fail-fast：put/remove/clear/putAll/跨视图→CME；未 next 先 remove→ISE；空迭代器 next→NSEE；值替换非结构修改 | testConcurrentModification、testIteratorsAndCME |
| P0 | Java 8 默认方法深分支：mapped-to-null、函数返 null 删映射、函数异常无副作用、computeIfAbsent 已存在不调函数、merge null 值 NPE、forEach(null) NPE、null key 全默认方法 | testJava8Defaults、testJava8DefaultsDeepNullSemantics |
| P0 | 状态不变量：删除/失败操作后逐项验证其余映射；clear 后逐 key 验证 | testStateInvariants |
| P0 | equals 跨实现（HashMap vs TreeMap/LinkedHashMap）对称 + hashCode | testEqualsHashCode、testEqualsCrossImplAndToString |
| P1 | toArray 家族：引用复用、null 终止符、精确长度、空视图 | testToArrayContract |
| P1 | null key × 扩容；迭代中 put 覆盖不重不漏 | testResizeNullKey、testNullKeyAtResize、testPutOverwriteDuringIteration |
| P1 | 碰撞树化：同 hash key 树化后 put(null)；树桶 iterator.remove + setValue 组合 | testTreeifyNullKey（hashCode=0）、testTreeifiedBins（hashCode=42） |
| P1 | 子类覆写 size/isEmpty 后各操作正常 | testSubclassOverrides |
| P1 | 扩容阈值边界（8/16/32/64）全量一致性 | testResizeThresholds |
| P1 | 序列化 round-trip（含 null kv）+ serialVersionUID | testSerialization、testSerializationRoundTrip |
| P2 | 视图 spliterator 特征（keySet/entrySet SIZED\|DISTINCT、values 仅 SIZED）、getComparator→ISE | testSpliteratorAndStreams、testViewSpliterators |
| P2 | 视图 stream/parallelStream 算子（无序断言） | testViewStreams |
| P2 | 视图 removeIf/forEach + 异常中继（含 Error） | testViewRemoveIfForEach |
| P2 | clear/clone 后 WeakReference 可回收（有界 GC 重试） | testClearReleasesReferences |

## 关键规范细节（易错，已锁入断言）

- **`merge` 的重映射函数只在 key 存在且值非 null 时被调用**；key 不存在时直接放入 value，函数不执行。
- `getOrDefault` 对「映射到 null」返回 null 而非默认值；`putIfAbsent` 把 null 值视为 absent。
- `computeIfAbsent`/`compute`/`computeIfPresent`/`merge` 函数返回 null → 删除映射；函数抛异常 → 映射不变。
- 视图 `add`/`addAll` → `UnsupportedOperationException`；`toArray(T[])` 容量更大时补 null 终止符。
- 值替换（put 同 key / replace / setValue）不是结构修改，不触发 CME。

## 注意事项

- 所有断言**不依赖 HashMap 迭代顺序**；无墙钟/随机/文件/网络依赖。
- P2 的 WeakReference 回收依赖 GC，做有界 10 次重试（极端环境误报可注释该段）。
- 有意不移植 OpenJDK 的 TreeBinAssert（需 `-esa`）、WhiteBoxResizeTest（反射私有字段）——实现细节导向，不适合 API 一致性套件。
- JDK-27 侧同目录额外加了反射白盒（extends AbstractMap / implements Map/Cloneable/Serializable）+ `newHashMap(int)` 工厂（JDK 19+）。
