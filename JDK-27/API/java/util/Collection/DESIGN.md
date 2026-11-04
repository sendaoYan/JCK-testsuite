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
