# java.util.concurrent.ConcurrentHashMap.KeySetView 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- `ConcurrentHashMap.newKeySet()` / `newKeySet(int)`：以 `Boolean.TRUE` 为默认值的并发 `Set<K>` 工厂。
- `map.keySet()` / `map.keySet(V mappedValue)`：map 的键视图；带默认值时 `add(e)` == `map.put(e, mappedValue)`。
- `getMap()`：返回底层 map（同一实例）。
- `getMappedValue()`：返回构造时给定的默认值（`newKeySet` 为 `Boolean.TRUE`，`keySet()` 为 null）。
- 继承 `CollectionView` 的 `contains/remove/containsAll/removeAll/retainAll/clear/iterator/size/spliterator`，
  全部写通道直通底层 map；实现 `Set` + `Serializable`。

## 2. 使用方式 + 开源用法调研

- `ConcurrentHashMap.newKeySet()` 是 JDK 8 推荐的并发 Set 写法（替代 `Collections.newSetFromMap`）。
- JDK 源码 `ConcurrentHashMap.java` javadoc 示例：`Set<String> set = ConcurrentHashMap.newKeySet();`。
- OpenJDK 回归 `test/jdk/java/util/concurrent/ConcurrentHashMap/KeySetView.java`：覆盖
  `keySet()`/`newKeySet` 的 add/remove/iteration 语义。
- 使用范式：并发去重集合、以固定默认值（如 `Boolean.TRUE`）把 Set 语义叠加在 CHM 上。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`ConcurrentHashMap.KeySetView`）。
- OpenJDK `test/jdk/java/util/concurrent/ConcurrentHashMap/`。
- 实测行为（JDK 8u372-b07 / JDK 27.0.1 双侧验证）：见第 4 部分易错点。

## 4. 测试用例设计（KeySetViewTest.java，PASS 34 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | newKeySet add/contains/remove/size 去重语义 | testKeySetView（保留） |
| P0 | keySet() 视图 contains/size 委托 | testKeySetView（保留） |
| P0 | `keySet(42).add` == `put(k,42)`、add 已存在键 false 且不覆盖 | testMappedValueSemantics |
| P0 | `getMappedValue()`（42 / Boolean.TRUE）、`getMap()` 同一实例 | testMappedValueSemantics |
| P0 | `keySet()`（null 默认值）视图 add → UnsupportedOperationException | testMappedValueSemantics |
| P0 | 视图 remove/size 联动底层 map | testMappedValueSemantics |
| P1 | containsAll/removeAll/retainAll/clear 写通道直通 | testViewIterationAndBulk |
| P1 | 迭代器弱一致：取迭代器后 put，继续遍历不抛 CME | testViewIterationAndBulk |
| P1 | `contains(null)`/`remove(null)` → NPE（委托 containsKey(null)） | testViewIterationAndBulk |
| P1 | 序列化 round-trip（含默认值、复活后可 add） | testSerialization |

### 易错规范细节（实测记录）

- `keySet()`（无参）返回的视图 `add` 抛 `UnsupportedOperationException`；`keySet(v)` 返回的视图 `add`
  等价于 `putIfAbsent(k, v)`：键已存在返回 false 且**不覆盖**原值。
- `newKeySet()` 的 `getMappedValue()` 是 `Boolean.TRUE`；`keySet()` 的是 null。
- 反序列化后 `getMappedValue()` 语义值等于 TRUE 但**不是同一个实例**（用 equals 比较，不能用 ==）。
- `contains(null)`/`remove(null)` 委托 `map.containsKey(null)` → NPE（CHM 系 null 全拒）。
