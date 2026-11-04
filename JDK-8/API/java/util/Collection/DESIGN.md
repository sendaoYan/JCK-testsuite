# java.util.Collection 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。add/remove/contains/size/isEmpty/clear、addAll/containsAll/removeAll、
> toArray 两变体、iterator、Java 8 默认方法 forEach/removeIf/stream/parallelStream/spliterator。

## 1. API 提供了哪些功能

- **核心**：`add/remove/contains/size/isEmpty/clear`、`addAll/containsAll/removeAll`、`toArray`。
- **默认方法（Java 8）**：`removeIf`、`stream`、`parallelStream`、`spliterator`、`forEach`（Iterable）。

## 2. Java 用户如何使用

- `collection.removeIf(pred)` 就地过滤；`collection.stream().filter(...)` 流式处理；
  `collection.spliterator()` 切分并行遍历。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Collection.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Collection.java`。

## 4. 测试用例设计（映射到 CollectionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基本操作 | testBasics |
| P0 | 批量操作 | testBulk |
| P0 | toArray | testToArray |
| P0 | iterator + forEach/removeIf | testIterationAndDefaults |
| P0 | stream/parallelStream/spliterator | testStreamDefaults |
| P0 | removeIf 边界 | testRemoveIfEdge |

## 关键规范细节（易错，已锁入断言）

- `removeIf` 有元素被移除返回 true，否则 false。
- `stream` 保持遇到顺序（顺序集合）；`parallelStream` 结果与 `stream` 一致（reduce 语义）。
- `spliterator` 报告精确大小（有大小集合）。

## 注意事项

- 无 GC/图形/网络/文件依赖；用 ArrayList（有序）为载体断言顺序。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testEqualsContract` — 跨实现 equals：ArrayList==LinkedList==Arrays.asList（含 hashCode 相等）；
  List 与 Set 同元素**永不** equals；顺序敏感。
- `testToArrayContract` — toArray(T[]) 全契约：够长**复用同一数组（==）**并在 size 处置 null、
  恰好、太小新分配、`toArray(null)` NPE、raw 类型下 String[] 收 Integer → **ArrayStoreException**。
- `testBulkReturnSemantics` — addAll/removeAll/retainAll 返回值（空/不相交/超集均 false）+ equals
  匹配语义（new String("a") 命中）。
- `testNullAndExceptionSemantics` — forEach/removeIf/addAll/containsAll 传 null → NPE；
  removeIf 谓词异常**原对象透传**，ArrayList 两段式实现下列表保持不变（8u/27 实测一致）。
- `testSpliteratorContract` — ArrayList spliterator ORDERED|SIZED|SUBSIZED、getExactSizeIfKnown
  精确、tryAdvance 顺序消费并递减、trySplit 前缀+后缀按序并集==原列表、HashSet DISTINCT。
- `testDefaultDispatchProbe` — 记录型最小实现（仅 iterator()+size()，add 保持骨架默认）：证明
  contains/remove/removeAll/retainAll/clear/removeIf 全部经 iterator（计数器锁定 remove 次数）、
  toString 对 null 元素渲染 "[x, null]"、默认 add UOE、未覆写 equals 时保持恒等语义。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| toArray((T[])null) | NullPointerException |
| raw.toArray(new String[5])（元素为 Integer） | ArrayStoreException |
| forEach(null)/removeIf(null)/addAll(null)/containsAll(null) | NullPointerException |
| 骨架默认 add（未覆写） | UnsupportedOperationException |
| 迭代器 remove 无前置 next | IllegalStateException |

### 本侧版本说明
- JDK-8 侧无 toArray(IntFunction)（JDK-11+）；removeIf/spliterator/stream 为接口默认（javap 不带 default 仅为显示差异）。
