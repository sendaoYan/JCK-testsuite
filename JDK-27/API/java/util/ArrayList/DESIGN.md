# java.util.ArrayList 测试方案

对应测试：`ArrayListTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 构造器：`ArrayList()`、`ArrayList(int initialCapacity)`、`ArrayList(Collection<? extends E>)`
- 核心操作：`add(E)` / `add(int, E)` / `addAll(Collection)` / `addAll(int, Collection)`、
  `set(int, E)`、`remove(int)` / `remove(Object)`、`removeAll` / `retainAll` / `clear`
- 查询：`get(int)`、`contains(Object)`、`indexOf(Object)`、`lastIndexOf(Object)`、`size()`、`isEmpty()`
- 转换：`toArray()`、`toArray(T[])`、`toArray(IntFunction<T[]>)`、`clone()`、`subList(int, int)`
- 迭代：`iterator()`、`listIterator()` / `listIterator(int)`、`spliterator()`、`forEach(Consumer)`
- 批量改写/排序：`sort(Comparator)`、`removeIf(Predicate)`、`replaceAll(UnaryOperator)`
- 容量：`trimToSize()`、`ensureCapacity(int)`
- SequencedCollection（Java 21+）：`getFirst()` / `getLast()` / `addFirst(E)` / `addLast(E)` /
  `removeFirst()` / `removeLast()`

## 2. Java 用户如何使用

典型组合链路（黑盒视角）：

```java
ArrayList<String> list = new ArrayList<>();          // 默认构造
list.add("a"); list.add("b"); list.addAll(c);        // 建表
list.get(i); list.set(i, v); list.remove(i);          // 改表
list.contains(x); list.indexOf(x);                    // 查询
for (String s : list) { ... }                         // 迭代
List<String> sub = list.subList(from, to);            // 视图
list.sort(Comparator.naturalOrder());                 // 排序
list.removeIf(s -> s.isEmpty());                      // 过滤
```

构造方式：默认构造；指定初始容量避免扩容；从已有 `Collection` 拷贝。

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码 + test/jdk）；本次未执行网络检索，故以 JDK 自用为权威、逐条标注
文件与行号。

- `java/io/File.java:1090` — `List<String> v = new ArrayList<>();`（收集结果）
- `java/io/DeleteOnExitHook.java:75` — `ArrayList<String> toBeDeleted = new ArrayList<>(theFiles);`（从集合拷贝构造）
- `java/net/InMemoryCookieStore.java:61` — `cookieJar = new ArrayList<>();`（字段持有列表）
- `java/text/DecimalFormat.java:3394` — `positions = new ArrayList<>(2);`（预分配容量）
- `java/io/ObjectOutputStream.java:2260` — `stack = new ArrayList<>();`（用作栈结构）

结论：ArrayList 主要作为“动态数组 / 结果容器 / 栈”，典型用法是默认构造 + add 收集 + 遍历；容量
预分配出现在高频小集合场景。

## 4. 测试用例设计 + 自动化实现

由 1–3 推导的测试场景（全部落在 `ArrayListTest.java` 黑盒部分）：

- 正常：构造/容量/集合拷贝构造；`add/addAll` 顺序；`get/set/remove`；`contains/indexOf/lastIndexOf`。
- 组合链路：`add/addAll → get/set → remove/removeAll/retainAll → contains/indexOf/lastIndexOf`；
  `iterator/listIterator` 遍历 + `add/set/remove`；`subList` 视图双向联动；`toArray` 三种形态；
  `sort` + `spliterator` 切分；`removeIf/replaceAll` 批量改写。
- 边界/异常：越界 `get/set/remove/add(index)` → `IndexOutOfBoundsException`；
  结构修改后 `iterator/listIterator/subList` 访问 → `ConcurrentModificationException`；
  允许 null 元素；空表 `getFirst/getLast/removeFirst/removeLast` → `NoSuchElementException`。

健壮性（并入实现）：确定性固定输入、无墙钟/无随机；`subList`/迭代断言不依赖集合迭代顺序（ArrayList
为顺序结构）；无全局副作用；纯 headless，无 `@key`。

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
