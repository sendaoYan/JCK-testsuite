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
