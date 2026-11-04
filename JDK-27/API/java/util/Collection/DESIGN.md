# java.util.Collection — 测试设计案

## 1. API 功能
集合框架根接口：定义大小/查询/批量操作/迭代/流式访问的基本契约，`extends Iterable`。

- 查询：size/isEmpty/contains/containsAll。
- 修改：add/remove/addAll/removeAll/retainAll/clear。
- 迭代与数组：iterator/toArray()/toArray(T[])/toArray(IntFunction)（default）。
- 流与遍历：spliterator/stream/parallelStream（default）、forEach（Iterable）、removeIf（default）。

## 2. Java 用户如何使用
任何集合都用它做统一操作：add -> size/contains -> addAll/removeAll -> iterator/for-each ->
toArray -> stream/parallelStream -> removeIf 条件删除。
链路：new ArrayList() -> addAll -> containsAll -> removeIf -> stream 归约 -> spliterator 遍历。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/Collection.java`（本地 `~/git/jdk25u-dev`）：
  removeIf/stream/parallelStream/toArray(IntFunction) 为 default 方法——第 1/4/5 节对齐。
- OpenJDK `test/jdk/java/util/Collection/CollectionDefaults.java` 等：官方测试覆盖 default 方法。
- Spring/Guava/Netty 广泛使用 `Collection.stream()/removeIf`。

## 4. 测试用例设计 + 自动化实现（CollectionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 基础 | add/remove/size/isEmpty/contains/clear |
| 批量 | addAll/containsAll/removeAll |
| toArray 三态 | toArray()/toArray(T[])/toArray(IntFunction) |
| 迭代+default | iterator、forEach、removeIf |
| 流 | stream/parallelStream 归约 |
| spliterator | estimateSize/forEachRemaining |

共 6 组场景、约 22 个 check（`testBlackBox()`）。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testEqualsContract` — 跨实现 equals 矩阵（ArrayList/LinkedList/Arrays.asList/List.of 互等且
  hashCode 相等；List 永不等于 Set；equals(null) false）。
- `testToArrayContract` — toArray 全四形态：复用（==）+ size 位 null 终止、恰好、太小新分配、
  **toArray(IntFunction)**（含 null generator NPE）、raw 下组件类型不符 ASE。
- `testBulkReturnSemantics` / `testNullAndExceptionSemantics` — 返回值矩阵 + null NPE 矩阵 +
  removeIf 谓词异常原对象透传且列表不变（27 实测）。
- `testSpliteratorContract` — ORDERED|SIZED|SUBSIZED 精确位、tryAdvance/trySplit 前后缀按序并集、
  HashSet DISTINCT。
- `testDefaultDispatchProbe` — 记录型探针锁定骨架默认分派（contains/remove/clear/removeIf 计数器）。
- `testSequencedDefaults`（JDK-21+）— SequencedCollection 默认方法在 ArrayList 与 LinkedHashSet 上的
  行为：addFirst/addLast、getFirst/getLast、removeFirst/removeLast、reversed 视图（活、可逆）、
  **LinkedHashSet addFirst/addLast 对已存在元素重定位**、空集合 getFirst NSEE、
  **不可变空 List.of().removeFirst() 先报 UOE（先于 NSEE）**。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| toArray((T[])null) / toArray((IntFunction)null) | NullPointerException |
| raw.toArray(new String[5])（元素为 Integer） | ArrayStoreException |
| forEach(null)/removeIf(null)/addAll(null)/containsAll(null) | NullPointerException |
| 骨架默认 add（未覆写） | UnsupportedOperationException |
| 空 List.of().removeFirst()/removeLast() | UnsupportedOperationException（不可变先于空） |
| 空可变 List removeFirst()/removeLast()/getFirst()/getLast() | NoSuchElementException |

### JDK-8 vs JDK-27 差异（本类）
- 27 有 toArray(IntFunction<T[]>)（JDK-11+）；27 的 Collection 继承体系上新增 SequencedCollection
  超接口路径（ArrayList/LinkedHashSet 均实现 sequenced 方法）；其余行为两侧实测一致。
