# java.util.WeakHashMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**（弱引用 GC 时机除外，不断言）。基本 map 操作、构造器矩阵（负容量/
> loadFactor≤0/NaN IAE、null Map NPE）、null key/value 允许、视图 live（keySet/values/entrySet
> remove 回写）、equals/hashCode 顺序无关、putAll、结构事实（**非 Serializable、非 Cloneable**）、
> 状态不变量。

## 1. API 提供了哪些功能

- **构造器（4）**：`WeakHashMap()`、`WeakHashMap(int)`、`WeakHashMap(int,float)`、`WeakHashMap(Map)`。
- **核心操作**：`put/get/remove`、`containsKey/containsValue`、视图、`putAll/clear`。
- **弱引用语义**：key 由弱引用持有，无外部强引用时可被 GC 回收（时机不确定，测试不断言）。
- **契约继承**：extends `AbstractMap`、implements `Map`；**不实现** `Cloneable`/`Serializable`。

## 2. Java 用户如何使用

- `new WeakHashMap<>()` 建 key 可被 GC 回收的映射（缓存场景）→ `put/get/remove` →
  key 无强引用后等待 GC 自动清理。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/WeakHashMap.html`：弱引用 key、null 允许、GC 时机不确定。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/WeakHashMap.java`（构造器
  IAE/NPE、非 Cloneable/Serializable）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/WeakHashMap`。

## 4. 测试用例设计（映射到 WeakHashMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：基本 map 操作 | testWeakHashMap |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | null key/value 允许 | testNullSemantics |
| P0 | 视图 live + equals/hashCode 顺序无关 | testViewsAndEquals |
| P0 | 结构事实：非 Serializable、非 Cloneable | testStructuralFacts |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 允许 null key 与 null value（与 Hashtable 相反）。
- `WeakHashMap` 继承 `AbstractMap`，**不实现 Cloneable 且不可序列化**
  （`ObjectStreamClass.lookup` 返回 null；Cloneable 不可赋值）。
- `equals/hashCode` 与顺序无关（Map 契约）。
- **弱 key GC 时机不确定**：测试不断言 gc 后 `get(key)==null`（避免不稳定性）。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；无 serialVersionUID（非 Serializable）。
