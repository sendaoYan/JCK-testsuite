# java.util.EnumMap 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。keySet/values/entrySet 按枚举声明序（ordinal）迭代、与插入顺序无关、
> put/get/remove、null key 拒绝（put/putIfAbsent/computeIfAbsent/compute/merge NPE）vs null-key
> 优雅查询（get/remove/containsKey/containsValue/getOrDefault 返回 null/false/default）、null value
> 允许、构造器矩阵（null Class NPE、空 Map IAE、含 null key Map NPE、EnumMap/Map 拷贝）、默认方法
> （getOrDefault/putIfAbsent/replace/replace(K,old,new)/compute/merge/replaceAll/forEach）、视图 live、
> equals/hashCode 顺序无关 + 跨实现（HashMap）、clone 深拷贝、序列化 round-trip + serialVersionUID、
> 状态不变量。

## 1. API 提供了哪些功能

- **构造器（3）**：`EnumMap(Class)`、`EnumMap(EnumMap)`、`EnumMap(Map)`。
- **核心操作**：`put/get/remove`、`containsKey/containsValue`、`size/isEmpty/clear`、视图。
- **默认方法**：`getOrDefault/putIfAbsent/replace/compute/merge/replaceAll/forEach`。
- **契约继承**：extends `AbstractMap`、implements `Cloneable`/`Serializable`
  （`serialVersionUID == 458661240069192865L`）；`equals/hashCode` 顺序无关。

## 2. Java 用户如何使用

- `new EnumMap<>(Color.class)` 建以单一枚举为 key 的有序映射 → `put`（乱序也可）→ 视图/遍历按
  枚举声明序 → `get/remove` → `putIfAbsent/compute/merge` 组合 → `clone`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/EnumMap.html`：键序 = 枚举声明序、key 非 null、value 可 null。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/EnumMap.java`（typeCheck NPE、
  构造器校验、数组实现）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/EnumMap`。

## 4. 测试用例设计（映射到 EnumMapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：顺序/操作、null、构造器 | testOrderAndOps、testNullHandling、testConstructors |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | null key 拒绝（默认方法全扫） + 优雅查询 | testNullKeyRejection |
| P0 | 默认方法 | testDefaultMethods |
| P0 | 视图 live | testViews |
| P1 | clone 深拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 跨实现 equals/hashCode（HashMap） | testCrossImplEquals |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 迭代顺序恒等于枚举声明序（ordinal），与插入顺序无关；values 跟随 key 顺序。
- **null key 语义不对称**：`put/putIfAbsent/computeIfAbsent/compute/merge` 传 null key 抛 NPE
  （内部 `typeCheck` → `key.getClass()`）；而 `get/remove/containsKey/containsValue` 对 null
  **优雅返回 null/false**，`getOrDefault(null, d)` 返回默认值 d（这些走 `isValidKey` 短路）。
- null value 允许（`put(k, null)` 成功，`containsValue(null)` 可查到）。
- 构造器：`EnumMap(null Class)` 抛 NPE；`EnumMap(空 Map)` 抛 IAE；`EnumMap(含 null key 的 Map)` 抛 NPE。
- `equals/hashCode` 与顺序无关，且与 `HashMap` 内容相同时互相 equals、hashCode 相等。
- `clone` 是深拷贝（复制底层 value 数组），与原 map 结构独立。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
