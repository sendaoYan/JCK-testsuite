# java.util.concurrent.CopyOnWriteArraySet 测试方案

## 1. API 功能（按方法分组）
- 构造器：`CopyOnWriteArraySet()`、`(Collection)`。
- 集合操作：`add(E)`（去重）、`remove(Object)`、`contains(Object)`、`size/isEmpty/clear`。
- 批量：`addAll/containsAll/removeAll/retainAll`、`removeIf/forEach`。
- 遍历：`iterator()/spliterator()/toArray()`。
- 实现：extends `AbstractSet` + `Serializable`。内部基于 `CopyOnWriteArrayList`。

## 2. Java 用户如何使用
- 读多写少去重集合：`CopyOnWriteArraySet<T> s = new CopyOnWriteArraySet<>();`。
- 重复 `add` 返回 false（保持集合语义）。
- 迭代器是快照：创建后修改不影响迭代内容，不抛 CME。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:184-185`：`CopyOnWriteArraySet` 与 `CopyOnWriteArrayList` 同为 COW 并发集合。
- JDK 测试 `test/jdk/java/util/concurrent/CopyOnWriteArraySet/`：覆盖 set 语义、去重、equals、快照等。
- 使用范式：读多写少的去重集合（如服务端点列表）。

## 4. 测试用例设计 + 自动化实现
实现位置：`CopyOnWriteArraySetTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractSet`/implements `Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `add 去重 → contains → remove → size`。
  - `addAll` 去重、`containsAll`。
  - 迭代快照（创建后 add，迭代器见旧快照）。
  - `equals/removeAll/retainAll`。
- 健壮性：固定输入、无顺序依赖（先排序再断言）、无全局副作用、headless-safe。
