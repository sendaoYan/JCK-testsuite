# java.util.AbstractCollection — 测试设计案

## 1. API 功能
所有 Collection 实现的最小骨架：只要求实现 `iterator()` 与 `size()`，其余（isEmpty/contains/
toArray/add/remove/containsAll/addAll/removeAll/retainAll/clear/toString）由骨架基于迭代器提供。

- 抽象：`iterator()`、`size()`。
- 继承的批量操作：`isEmpty/contains/toArray()/toArray(T[])/add(E)（默认抛 UnsupportedOperationException）
  /remove(Object)/containsAll/addAll/removeAll/retainAll/clear/toString`。

## 2. Java 用户如何使用
自定义集合只写 iterator+size 即获得全部 Collection 能力；骨架的 `add` 对只读集合默认抛
UnsupportedOperationException，需自行覆写才可写。
链路：new 最小子类 -> add(覆写) -> size/contains/toArray -> remove/retainAll/clear -> toString。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/AbstractCollection.java`（本地 `~/git/jdk25u-dev`）：
  `add` 抛 `UnsupportedOperationException`；`toString` 用 iterator 拼 `[e1, e2]`；`toArray` 按迭代器
  顺序拷贝、数组够大时 null 截断——第 2/3 节断言与实现一致。
- OpenJDK `test/jdk/java/util/AbstractCollection/ToArrayTest.java`、`ToString.java`：官方测试专门
  覆盖 toArray 与 toString——本测试同样覆盖。
- Guava `ForwardingCollection` 等大量依赖 AbstractCollection 骨架实现派生视图。

## 4. 测试用例设计 + 自动化实现（AbstractCollectionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 空/增删 | 初始空、add 后 size/contains/containsAll |
| toArray | toArray() 迭代序；toArray(T[]) 复用/超长 null 截断/不足时分配 |
| remove | remove 存在删除、缺省返回 false、size 收缩 |
| addAll/retainAll | 返回值 true、元素变化 |
| toString | "[p, q]" |
| clear | 清空 |
| 只读子类 | 默认 add 抛 UnsupportedOperationException |

共 7 组场景、约 22 个 check（`testBlackBox()`）。
