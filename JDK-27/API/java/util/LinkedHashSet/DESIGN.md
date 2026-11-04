# java.util.LinkedHashSet 测试方案

## 1. API 功能（按方法分组）

- 构造器：`LinkedHashSet()`、`LinkedHashSet(int initialCapacity)`、`LinkedHashSet(int, float)`、`LinkedHashSet(Collection)`；静态 `newLinkedHashSet(int)`。
- 核心操作：`add`/`remove`/`contains`/`clear`（继承 HashSet，元素唯一）。
- 顺序（SequencedSet）：`addFirst(E)`、`addLast(E)`、`getFirst()`、`getLast()`、`removeFirst()`、`removeLast()`、`reversed()`。

## 2. Java 用户如何使用

- 构造：`new LinkedHashSet<>()`，需要“无重复 + 插入序”的有序集合。
- 典型调用：`add` 保持插入序；`addFirst`/`addLast` 调整位置；`getFirst`/`getLast`/`removeFirst`/`removeLast` 首尾操作；`reversed()` 倒序视图。
- 真实组合链路：
  1. add a,b,c → 重复 add 拒绝且不改变顺序 → add null 保持位置。
  2. addFirst z / addLast w → getFirst/getLast → removeFirst/removeLast → 顺序校验。
  3. addFirst 已存在元素（重新定位到最前）→ reversed 倒序视图 → equals/hashCode。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.ServiceLoader.java:1039`：`Set<String> names = new LinkedHashSet<>(); // preserve insertion order` —— 需保序去重。
- JDK 源码 `java.util.regex.Pattern.java:1665`：`Set<String> altns = new LinkedHashSet<>();` —— 收集正则分支并保序。
- JDK 测试 `test/jdk/java/util/zip/zip.java:48`：`Set<File> entries = new LinkedHashSet<>();`。

## 4. 测试用例设计 + 自动化实现

实现位置：`LinkedHashSetTest.java`（`testWhiteBox()` + `testInsertionOrderAndDedup()`/`testSequencedOps()`/`testEqualsHashCode()`）。

- 正常：插入序迭代、去重、null。
- 组合：SequencedSet 首尾操作 + reversed 视图 + addFirst 重定位。
- 边界/异常：add 重复返回 false；null 允许并保持位置。
- 健壮性：确定性（固定字符串）；无全局副作用；无文件/网络；跨平台。headless-safe。

## 契约深化补充（第二轮：重插入移尾/写通道/fail-fast/toArray 保序/equals+hashCode 锁/批量返回值/stream+精确特征位/含 null 序列化/SequencedSet 边界）

新增方法（只追加）：

| 测试点 | 用例方法 |
|---|---|
| remove+reinsert 移尾；重复 add 不重排 | testReinsertMovesToEnd |
| 迭代器写通道（iterator.remove 保序、ISE、空迭代器 NSEE、外部 clear 后 CME） | testIteratorWriteChannels |
| fail-fast（add/remove/clear 后 next CME；iterator.remove 合法） | testFailFast |
| toArray 保序契约（复用==、精确长度、null 终止符） | testToArrayOrder |
| hashCode 公式锁 + 跨实现 equals（HashSet/TreeSet）+ Set.equals(List) false | testEqualsHashCodeDeep |
| 批量操作返回值契约 + null NPE×4 | testBatchOpReturnContracts |
| stream 保序、spliterator 精确特征位 0x4051、trySplit 按序拼接==原序列 | testStreamAndSpliterator |
| 含 null 序列化保序、空集 round-trip | testSerializationWithNull |
| SequencedSet 边界：空集 getFirst/getLast/removeFirst/removeLast → NoSuchElementException；addFirst(null) 合法且居首；reversed() 活视图（removeFirst 删原末位、add 追加原末位、reversed().reversed() 复原、从 reversed 构造拷贝倒序） | testSequencedEdgeCases |

负例精确异常表（新锁定）：
- 迭代中外部结构修改 → next() CME；iterator.remove 未 next → ISE；空迭代器 next → NSEE
- 空集 getFirst/getLast/removeFirst/removeLast → NoSuchElementException（SequencedSet 契约）
- addAll/containsAll/removeAll/retainAll(null) → NPE

JDK-8 vs JDK-27 差异：JDK 21+ LinkedHashSet 实现 SequencedSet（addFirst 重定位既有元素、reversed 活视图、newLinkedHashSet(int) 工厂）——27 侧断言、8 侧无此 API；其余契约双侧一致。
