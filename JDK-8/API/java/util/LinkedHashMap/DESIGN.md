# java.util.LinkedHashMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。默认插入序迭代（重复 put 不改变位置、get 不重排）、accessOrder=true 时
> 访问序（get/put 更新均算访问并移到末尾）、removeEldestEntry LRU 淘汰、构造器矩阵（负容量/
> loadFactor≤0/NaN IAE、null Map NPE、Map 拷贝保留源顺序）、null key/value 允许、clone 浅拷贝、
> 序列化 round-trip + serialVersionUID、equals/hashCode 顺序无关 + 跨实现（HashMap）、状态不变量。

## 1. API 提供了哪些功能

- **构造器（5）**：`LinkedHashMap()`、`LinkedHashMap(int)`、`LinkedHashMap(int,float)`、
  `LinkedHashMap(int,float,boolean accessOrder)`、`LinkedHashMap(Map)`。
- **核心操作**：`put/get/remove`（继承 HashMap），维护可预测迭代顺序。
- **可覆写钩子**：`protected boolean removeEldestEntry(Map.Entry eldest)`（默认 false，用于 LRU）。
- **契约继承**：extends `HashMap`、implements `Map`/`Cloneable`/`Serializable`
  （`serialVersionUID == 3801124242820219131L`）；`equals/hashCode` 顺序无关（Map 契约）。

## 2. Java 用户如何使用

- `new LinkedHashMap<>()` 建插入序映射 → `put/get` → 按插入序遍历 →
  `new LinkedHashMap<>(16, .75f, true)` 建访问序（LRU 基础）→ 子类覆写 `removeEldestEntry`
  做容量淘汰。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/LinkedHashMap.html`：插入序/访问序、removeEldestEntry、
  null 允许。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/LinkedHashMap.java`（构造器
  IAE/NPE、afterNodeAccess 重排、removeEldestEntry 钩子）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/LinkedHashMap`。

## 4. 测试用例设计（映射到 LinkedHashMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：插入序、访问序、LRU 淘汰 | testInsertionOrder、testAccessOrder、testRemoveEldestEntry |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | null key/value 允许 | testNullAllowance |
| P1 | clone 浅拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 跨实现 equals/hashCode（HashMap） | testCrossImplEquals |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 插入序下重复 `put(existing)` 更新值但**不改变位置**；`get` 不重排。
- 访问序（accessOrder=true）下 `get(existing)` 与 `put(existing)` 更新都算访问，把条目移到末尾；
  新 key 追加到末尾。
- `removeEldestEntry` 默认返回 false；子类覆写后每次 put 后回调，返回 true 则淘汰最老条目。
- 构造器：`LinkedHashMap(-1)`、`loadFactor≤0 或 NaN` 抛 IAE；`LinkedHashMap(Map null)` 抛 NPE；
  `LinkedHashMap(Map)` 保留源 Map 的相遇顺序。
- 允许 null key/value（与 HashMap 一致）。
- `equals/hashCode` 与顺序无关，与 `HashMap` 内容相同时互相 equals、hashCode 相等。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 深化追加（契约级，第二轮，JDK-8）

新增 testComputeMergeMatrix / testViewWriteChannels / testAccessOrderFineGrained /
testHashCodeFormula / testSerializationAccessOrder：

- **compute-merge 全分支矩阵**：putIfAbsent（present 保旧返回旧值/absent 插入）、remove(k,v)
  （值不匹配 false 不动条目/匹配才删）、replace(K,V)（absent 返回 null 且不插入）、
  replace(K,old,new)（旧值精确匹配）、getOrDefault 双分支；computeIfAbsent（absent 插入、
  present **不调函数**（计数器证明）、函数返回 null 不落 entry 且 containsKey false）、
  computeIfPresent（present 计算、absent 不调函数、返回 null 删 entry）、compute（absent 插入
  v==null/present 重算/返回 null 删）、merge（absent 直接放 value 不调函数/present remap/
  返回 null 删）；**每个映射函数异常分支：同一异常实例原样透传（assertSame 语义）且条目不变**；
  null remappingFunction / merge null value → NPE。
- **视图写通道 + fail-fast**：keySet/values/entrySet 三个视图的 iterator.remove 透传删映射、
  entry.setValue 反写并返回旧值；三个视图 add → UOE；迭代中结构性 put → CME；
  **iterator.remove 是迭代中唯一合法结构修改**（之后继续 next 不抛）；entry.setValue 属非结构
  修改（迭代中 setValue 后继续 next 不抛 CME）。
- **访问序细粒度**：putIfAbsent(present)/getOrDefault/merge(present)/compute(present) 均记录
  访问（重排）；containsKey/containsValue **不**记录访问。
- **hashCode 公式锁定**：Map.hashCode = Σ (k.hashCode ^ v.hashCode)；{a=1,b=2} == 192 字面量
  锁定；null value 贡献 k.hashCode ^ 0。
- **序列化保序 accessOrder**：accessOrder=true 序列化 round-trip 后 get 仍会重排 → 标志位被
  序列化保留。
