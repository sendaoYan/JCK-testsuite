# java.util.Vector 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵（负容量 IAE、null Collection NPE、0 容量可用、拷贝）、
> 默认容量 10 + 扩容规则（capacityIncrement>0 加增量、=0 翻倍、ensureCapacity/trimToSize）、
> add/get/set/remove、indexOf/lastIndexOf(fromIndex)/contains、遗留 addElement/removeElement/
> elementAt/firstElement/lastElement/insertElementAt/setElementAt/removeElementAt/elements()、
> copyInto/toArray 契约、setSize（填 null/截断）、subList 视图（live+fail-fast）、迭代器
> fail-fast、null 元素允许、clone 浅拷贝、序列化 round-trip + serialVersionUID、跨实现 equals
> （与 ArrayList）、状态不变量。

## 1. API 提供了哪些功能

- **构造器（4）**：`Vector()`、`Vector(int)`、`Vector(int,int)`、`Vector(Collection)`。
- **核心操作**：`add/get/set/remove`、`indexOf/lastIndexOf(含 fromIndex)/contains`、`size/isEmpty/clear`。
- **遗留操作**：`addElement/removeElement/removeElementAt/removeAllElements/elementAt/
  firstElement/lastElement/insertElementAt/setElementAt/copyInto/elements()`。
- **容量**：`capacity()/ensureCapacity(int)/trimToSize()`；`setSize(int)` 填 null 或截断。
- **契约继承**：extends `AbstractList`、implements `List`/`RandomAccess`/`Cloneable`/`Serializable`
  （`serialVersionUID == -2767605614048989439L`）；`equals/hashCode` 满足 List 契约（内容等价，
  顺序无关跨实现）。

## 2. Java 用户如何使用

- `new Vector<>(int,int)` 建线程安全可扩容列表 → `add/addElement` 填充 → `get/elementAt` 查询 →
  `indexOf/lastIndexOf` 定位 → `remove/removeElement` 删除 → `copyInto` 拷出 → `elements()` 枚举 →
  `setSize` 调整 → `subList` 取视图。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Vector.html`：默认容量 10、扩容规则、synchronized。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Vector.java`（构造器 IAE、
  copyInto 越界 AIOOBE、capacityIncrement 语义）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/Vector`。

## 4. 测试用例设计（映射到 VectorTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：基础 add/get/set/remove、遗留方法、clear/迭代 | testBasics、testLegacy、testClearIteration |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | 容量与扩容（增量/翻倍/ensureCapacity/trimToSize） | testCapacityGrowth |
| P0 | copyInto/toArray 契约 | testCopyIntoAndToArray |
| P0 | setSize 填 null/截断 | testSetSize |
| P0 | elements() 枚举顺序 | testEnumeration |
| P0 | subList 视图 live + fail-fast | testSubListView |
| P0 | 迭代器 fail-fast | testIteratorFailFast |
| P0 | null 元素允许（与 Hashtable 对照） | testNullAllowance |
| P1 | clone 浅拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 跨实现 equals/hashCode（与 ArrayList） | testCrossImplEquals |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `Vector(-1)` 与 `Vector(-1, 0)` 抛 `IllegalArgumentException`；`Vector(Collection)` null 抛 NPE；
  `Vector(0)` 可用。
- **允许 null 元素**（与 Hashtable 禁止 null 对照）：`add/addElement/insertElementAt` 接受 null，
  `contains/indexOf/lastIndexOf` 正常查询。
- 扩容：`capacityIncrement > 0` 时每次加该增量（`new Vector(3,5)` 满后 3→8）；
  `capacityIncrement == 0`（或构造未指定）时翻倍（2→4）。
- `copyInto` 目标数组过小抛 `ArrayIndexOutOfBoundsException`；`toArray(T[])` 数组足够大时复用并补 null。
- `setSize` 变大填 null、变小截断。
- `subList`/迭代器是 fail-fast（结构修改后抛 `ConcurrentModificationException`）。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
