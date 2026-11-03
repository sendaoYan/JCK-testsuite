# java.util.concurrent.CopyOnWriteArrayList 测试方案

## 1. API 功能（按方法分组）
- 构造器：`CopyOnWriteArrayList()`、`(Collection)`、`(E[])`。
- 列表操作：`add(E)`、`add(int,E)`、`get(int)`、`set(int,E)`、`remove(int)`、`remove(Object)`。
- 查询：`size/isEmpty/contains/indexOf/lastIndexOf`。
- 特有：`addIfAbsent(E)`、`addAllAbsent(Collection)`。
- 批量/默认方法：`forEach/removeIf/replaceAll/sort`。
- 视图：`subList(int,int)`、`iterator()`、`reversed()`。
- 实现 `List` + `RandomAccess` + `Cloneable` + `Serializable`。写时复制（COW）。

## 2. Java 用户如何使用
- 读多写少集合：`CopyOnWriteArrayList<T> l = new CopyOnWriteArrayList<>();`。
- 写操作复制底层数组，读操作无锁。
- 迭代器是快照：创建迭代器后的修改不影响迭代器可见内容（不抛 CME）。
- `l.addIfAbsent(e)` 去重插入；`l.addAllAbsent(c)` 批量去重。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:184-190`：`CopyOnWriteArrayList`/`CopyOnWriteArraySet` 是并发集合，且 COW 列表相比 `synchronized` ArrayList 更适用于读多写少。
- JDK 测试 `test/jdk/java/util/concurrent/CopyOnWriteArrayList/` 与 `CopyOnWriteArraySet/`：覆盖 COW 快照、addIfAbsent、equals 等契约。
- 使用范式：事件监听器列表、配置快照、读远多于写的集合。

## 4. 测试用例设计 + 自动化实现
实现位置：`CopyOnWriteArrayListTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + implements `List`/`RandomAccess`/`Cloneable`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `add → get → set → indexOf/contains → remove(Object)/remove(int) → clear`。
  - `addIfAbsent/addAllAbsent` 去重。
  - 迭代快照：迭代器创建后 `add/set`，迭代器仍见旧快照，列表见新状态。
  - `subList`、`sort/replaceAll`、`equals`。
- 健壮性：固定输入、快照语义断言、无全局副作用、headless-safe。
