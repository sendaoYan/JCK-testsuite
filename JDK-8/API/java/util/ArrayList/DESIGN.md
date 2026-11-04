# java.util.ArrayList 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵（负容量 IAE、null Collection NPE、拷贝独立）、add/get/set/remove
> （含 null、越界 IOBE）、contains/indexOf/lastIndexOf、批量 addAll/removeAll/retainAll/containsAll、
> toArray 三变体（含 pad 尾部 null）、iterator/listIterator 顺序与 fail-fast（CME）、5 条结构性写通道
> 引发的 CME（remove(int)/clear/add/subList.clear/iterator.remove）、listIterator 边界（-1/size+1 IOBE、
> nextIndex/previousIndex、remove 前置 ISE）、subList 视图双向联动 + 边界（负/超界 IOBE、from>to IAE、
> 嵌套 subList）、clone 浅拷贝、equals/hashCode、List 默认方法 sort/replaceAll/removeIf/forEach、
> null 参数 NPE（addAll/containsAll/removeAll/retainAll）、序列化 round-trip + serialVersionUID、
> 状态不变量。

## 1. API 提供了哪些功能

- **构造器（3）**：`ArrayList()`、`ArrayList(int initialCapacity)`、`ArrayList(Collection)`。
- **核心操作**：`add/addAll`、`get/set`、`remove/removeAll/retainAll/clear`、`contains/indexOf/lastIndexOf`、
  `toArray`、`iterator/listIterator`、`subList`、`clone`、`trimToSize/ensureCapacity`。
- **List 默认方法**：`sort`、`replaceAll`、`removeIf`、`forEach`。
- **契约继承**：extends `AbstractList`、implements `List`/`RandomAccess`/`Cloneable`/`Serializable`
  （`serialVersionUID == 8683452581122892189L`）。

## 2. Java 用户如何使用

- `new ArrayList<>()` → `add/addAll` 建表 → `get/set/remove` 改表 → `iterator/listIterator` 遍历 →
  `subList` 视图 → `sort` 排序。索引越界抛 IOBE；结构修改后迭代器抛 CME。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/ArrayList.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/ArrayList.java`
  （`Itr.remove` 无前置 next 抛 ISE；`checkForComodification` 抛 CME；subList 范围检查）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/ArrayList`。

## 4. 测试用例设计（映射到 ArrayListTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：构造/增删改查/contains/bulk/toArray/iterator/listIterator/subList/clone/clear | testConstruction…testClearCapacity |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | 多通道 fail-fast CME | testFailFastChannels |
| P0 | listIterator 边界 | testListIteratorEdges |
| P0 | subList 边界 + 嵌套视图 | testSubListBounds |
| P0 | null 参数 NPE | testNullArgs |
| P0 | List 默认方法 sort/replaceAll/removeIf/forEach | testListDefaultMethods |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |

## 关键规范细节（易错，已锁入断言）

- `listIterator(index)` 在 `index==-1` 或 `index==size+1` 抛 IOBE；`remove()` 无前置 `next()/previous()` 抛
  **IllegalStateException**（不是 CME/IOBE）。
- `subList(from>to)` 抛 **IllegalArgumentException**；`subList(-1,·)`/`subList(·, size+1)` 抛 IOBE。
- fail-fast：结构性修改（`remove(int)`/`clear`/`add`/`subList.clear`/迭代器自身 remove 后的再次访问）都使
  已激活迭代器抛 CME。
- null 元素允许；`addAll(null)`/`addAll(i,null)`/`containsAll(null)`/`removeAll(null)`/`retainAll(null)`
  均抛 NPE（AbstractCollection 的 `requireNonNull(c)`）。
- `clone` 浅拷贝：结构独立，元素对象共享。

## 注意事项

- 无 GC/图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 深化批次 II（契约级追加，仅追加不删既有断言）

- **hashCode 公式锁定**：`List.hashCode = 1; 31*r+hash(e)`——`[]`=1、`[a]`=128、`[a,b]`=4066、
  `[a,b,null]`=126046、`[null]`=31（null 贡献 0）。
- **跨实现 equals 全对称**：ArrayList/LinkedList/Vector/Stack/Arrays.asList 同内容 equals 且 hashCode 相同。
- **toArray(T[]) 身份契约**：数组够长→返回**同一实例**（`ret == a`）且 `a[size]` 置 null；空列表配大数组→`a[0]` 置 null。
- **clone 浅拷贝**：元素共享（StringBuilder 突变互见）、结构独立、null 元素保留。
- **迭代器状态机**：未 next 先 remove→ISE；双 remove→ISE；`iterator.remove()` 是迭代中唯一合法结构修改；
  forEachRemaining 只访问剩余并耗尽 hasNext；空迭代器 next→NSEE。
- **subList 活视图**：add/remove/set/iterator.remove 全部透传父表；subList equals/hashCode 按自身内容计算；
  父结构修改后子表 get/size/iterator/add 全部 CME；子表自身的修改不失效自己。
- **越界矩阵**：add(-1)/add(size+1)/addAll(-1)/addAll(size+1)→IOOBE，失败操作不留痕。
