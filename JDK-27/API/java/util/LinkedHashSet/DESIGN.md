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
