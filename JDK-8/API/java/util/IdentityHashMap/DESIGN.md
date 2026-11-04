# java.util.IdentityHashMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。引用相等（==）key 语义（get/containsKey/remove 按引用命中）、
> 引用相等 value 语义（containsValue 按 ==；注意 JDK-8 的 remove(K,V)/replace(K,old,new) 走
> Map 默认 equals 语义，未覆写）、内容相等但引用不同的 key 视为不同条目、null key 允许、构造器
> 矩阵（负容量 IAE、null Map NPE）、视图 live、equals/hashCode 按引用、clone 浅拷贝、序列化
> round-trip + serialVersionUID（含**序列化破坏引用相等**的 quirk）、状态不变量。

## 1. API 提供了哪些功能

- **构造器（3）**：`IdentityHashMap()`、`IdentityHashMap(int expectedMaxSize)`、`IdentityHashMap(Map)`。
- **核心操作**：`put/get/remove`、`containsKey/containsValue`、视图、`putAll/clear/clone`。
- **身份语义**：key 用 `==` 比较；`containsValue` 用 `==`；`equals` 用 `==`。
- **契约继承**：extends `AbstractMap`、implements `Map`/`Cloneable`/`Serializable`
  （`serialVersionUID == 8188218128353913216L`）。

## 2. Java 用户如何使用

- 需要按对象身份（而非 equals）区分 key 时（如拦截器、缓存）用 `IdentityHashMap`：
  `put(keyObject, value)` → `get(keyObject)` 按同一引用命中。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/IdentityHashMap.html`：== 语义、null key 允许、
  与普通 Map 的 equals 契约可能违背。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/IdentityHashMap.java`
  （containsValue 用 ==；remove(K,V)/replace(K,old,new) **未覆写**，走 Map 默认 equals）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/IdentityHashMap`。

## 4. 测试用例设计（映射到 IdentityHashMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：身份 key 语义 | testIdentitySemantics |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | 身份 value 语义（containsValue ==） | testValueIdentity |
| P0 | null key + 视图 live | testNullKeyAndViews |
| P0 | equals 按引用 | testEqualsUsesIdentity |
| P1 | clone 浅拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID（引用相等被破坏） | testSerialization |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- key 用 `==`：内容相等但引用不同的两个 String 是两个独立 key；`get(new String("x"))` 返回 null。
- `containsValue` 在 JDK-8 **已覆写为 ==**（引用相等），内容相等但引用不同的 value 查不到。
- **JDK-8 差异**：`remove(K,V)`、`replace(K,old,new)` 走 Map 默认实现，用 `equals` 比较 value，
  **不是**引用相等（JDK-27 已覆写为 ==）。本测试只锁定 containsValue 的 == 语义，避免版本漂移。
- null key 允许（`get(null)`/`containsKey(null)` 正常）。
- `equals/hashCode` 按引用：只有同一引用 key/value 的两个 map 才 equals。
- **序列化破坏引用相等**：序列化后 key 反序列化为新引用，因此 round-trip 结果与原 map
  `!equals`、`get(原引用)==null`；只能通过迭代取得反序列化后的 key（新引用）。
- 构造器：`IdentityHashMap(-1)` 抛 IAE；`IdentityHashMap(null Map)` 抛 NPE。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
