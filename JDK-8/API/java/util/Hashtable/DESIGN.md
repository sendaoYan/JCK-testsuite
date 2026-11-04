# java.util.Hashtable 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵（负容量/loadFactor≤0/NaN → IAE、0 容量可用、null Map/
> 含 null 键值 Map → NPE、拷贝）、null 拒绝（所有入口含被覆写的 8 个默认方法 NPE，但 merge 的
> null value 例外）、contains/containsKey/containsValue + 遗留 contains() 别名、keys()/elements()
> Enumeration（无序、**非 fail-fast**）、视图 live（keySet/values/entrySet 增删改回写）+ 视图迭代器
> fail-fast CME、equals/hashCode 顺序无关 + 跨实现（与 HashMap）、clone 浅拷贝、序列化 round-trip
> + serialVersionUID、状态不变量。

## 1. API 提供了哪些功能

- **构造器（4）**：`Hashtable()`、`Hashtable(int)`、`Hashtable(int,float)`、`Hashtable(Map)`。
- **核心操作**：`put/get/remove`、`contains`（值是别名 containsValue）、`containsKey/containsValue`、
  `keys()/elements()`（遗留 Enumeration）、`keySet/values/entrySet` 视图、`putAll/clear/clone`。
- **Java 8 默认方法覆写**（全部 synchronized + null 拒绝）：`getOrDefault/forEach/replaceAll/
  putIfAbsent/remove(K,V)/replace(K,V)/replace(K,V,V)/computeIfAbsent/computeIfPresent/compute/merge`。
- **契约继承**：extends `Dictionary`、implements `Map`/`Cloneable`/`Serializable`
  （`serialVersionUID == 1421746759512286392L`）；`equals/hashCode` 与顺序无关。

## 2. Java 用户如何使用

- `new Hashtable<>()` 建线程安全映射 → `put/get/remove` → `containsKey/containsValue` →
  `keys()/elements()` 遍历 → 视图增删 → `putIfAbsent/compute/merge` 原子组合。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Hashtable.html`：禁止 null key/value、synchronized、
  Enumeration 遗留、fail-fast 弱保证。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Hashtable.java`（逐方法核对
  `Objects.requireNonNull` 与 Enumerator fail-fast 差异）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/Hashtable`。

## 4. 测试用例设计（映射到 HashtableTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：put/get/remove、null 拒绝、contains 别名、视图、equals/hashCode | testPutGetRemove、testNullRejection、testContains、testViews、testEqualsHashCode |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | 默认方法全 null 拒绝 | testNullRejectionExtended |
| P0 | Enumeration 无序 + **非 fail-fast** | testEnumerationAndFailFast |
| P0 | 视图 live + 视图迭代器 fail-fast | testViewLive |
| P1 | clone 浅拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 跨实现 equals/hashCode（与 HashMap） | testCrossImplEquals |
| P1 | 状态不变量 + merge null value 例外 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 禁止 null key 与 null value：`put/get/contains/containsKey/containsValue/remove` 以及所有被覆写的
  默认方法对 null 抛 NPE；`Hashtable(Map)` 遇 null key 或 null value 抛 NPE。
- **`Hashtable.merge(key, null, fn)` 是例外**：源码只 `Objects.requireNonNull(remappingFunction)`，
  对 null value **不抛 NPE**——key 存在时返回/保留 remapping 结果，key 不存在时返回 null 且不插入。
  `merge(key, value, null)` 抛 NPE（remappingFunction 为 null）。
- `contains(Object)` 是 `containsValue` 的别名（不是 containsKey）。
- **Enumeration 非 fail-fast**：`keys()/elements()` 返回的 Enumerator 只在 `next()/remove()`
  （Iterator 路径）检查 modCount；`nextElement()` **不检查** modCount，因此结构修改后仍可继续遍历、
  不抛 CME。视图迭代器（`keySet().iterator()` 等）走 Iterator 路径，是 fail-fast 的。
- 构造器：`Hashtable(-1)` 与 `loadFactor<=0 或 NaN` 抛 IAE；`Hashtable(0)` 可用。
- `equals/hashCode` 与顺序无关，且 `Hashtable` 与 `HashMap` 内容相同互相 equals、hashCode 相等。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 深化追加（契约级，第二轮，JDK-8）

新增 testComputeMergeMatrix / testDictionaryDispatch / testViewIteratorsExtended /
testHashCodeFormula：

- **compute-merge 全分支矩阵**：putIfAbsent/remove(k,v)/replace 两形态/getOrDefault/
  computeIfAbsent/computeIfPresent/compute/merge 全分支（含 present 不调函数计数器、函数返回
  null 的删/不插语义、**异常同实例透传且状态不变**）；Hashtable 特有：computeIfAbsent(null key)
  在函数执行前抛 NPE（计数器证明函数未被调用）；containsValue(null) NPE；merge(null key) NPE。
- **Dictionary 继承分派**：以 Dictionary 引用调用 size/isEmpty/put/get/remove/keys/elements 全部
  正确分派且修改互通；空表 keys()/elements() hasMoreElements==false。
- **视图迭代器扩展**：values/entrySet 迭代器 remove 透传；values/entrySet 迭代器结构修改后
  next → CME；迭代中 entry.setValue 非结构修改不抛 CME 且逐条反写。
- **hashCode 公式锁定**：{a=1,b=2}.hashCode == 192（Σ k^v 公式）。
