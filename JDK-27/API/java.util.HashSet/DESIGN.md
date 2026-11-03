# java.util.HashSet 测试方案

## 1. API 功能（按方法分组）

- 构造器：`HashSet()`、`HashSet(int initialCapacity)`、`HashSet(int, float)`、`HashSet(Collection)`。
- 核心操作：`add(E)`、`remove(Object)`、`clear()`。
- 查询：`contains(Object)`、`size()`、`isEmpty()`。
- 视图/迭代：`iterator()`、`spliterator()`、`toArray`。
- 集合运算：`addAll`/`removeAll`/`retainAll`/`containsAll`/`removeIf`（继承自 Set/Collection）。

## 2. Java 用户如何使用

- 构造：`new HashSet<>()`；`new HashSet<>(Arrays.asList(...))` 或 `new HashSet<>(collection)` 去重。
- 典型调用：`add` 去重（重复返回 false）、`contains`、`remove`、集合运算 `addAll/removeAll/retainAll`。
- 真实组合链路：
  1. add 重复 + null → size 只计唯一元素。
  2. equals 相等的对象去重（依据 equals/hashCode）。
  3. containsAll/removeAll/addAll/retainAll/removeIf。
  4. equals/hashCode 顺序无关；迭代（排序后比较，因无序）；结构修改 fail-fast。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.ServiceLoader.java:803`：`Set<ModuleLayer> visited = new HashSet<>();` 记录已访问层。
- JDK 源码 `java.util.ServiceLoader.java:992`：`Set<String> providerNames = new HashSet<>(); // to avoid duplicates` 用 HashSet 去重。
- JDK 源码 `java.util.Arrays.java:5011`：`deepToString(a, buf, new HashSet<>())` 用 HashSet 追踪已展开数组防循环。
- JDK 源码 `java.util.Collections.java:3397-3402`（javadoc 代码片段）：`new HashSet<>()` 作为类型推断示例。

## 4. 测试用例设计 + 自动化实现

实现位置：`HashSetTest.java`（`testWhiteBox()` + `testAddRemoveContains()`/`testDedupByEqualsHashCode()`/`testCollectionOps()`/`testEqualsHashCode()`/`testIterationAndFailFast()`）。

- 正常：add/remove/contains/size/clear。
- 组合：equals 去重 + 集合运算 + equals/hashCode 顺序无关。
- 边界/异常：null 允许；add 重复返回 false；迭代器结构修改抛 CME。
- 健壮性：无序迭代先排序再比较（不依赖 HashSet 迭代顺序）；无墙钟/随机/全局副作用；无文件/网络；跨平台。headless-safe。
