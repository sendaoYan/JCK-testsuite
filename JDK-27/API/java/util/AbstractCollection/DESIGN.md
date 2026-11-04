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

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法与探针
- `Rec`（计数探针）→ `testDefaultDispatchRecording`：contains/remove/removeAll/retainAll/clear/
  removeIf 的骨架分派全部经 iterator（计数器锁定）；null 元素 == 分支；toString "[x, null]"。
- `Fixed`（只读迭代器探针）→ `testUoePropagationFromIterator`：UOE 透传且集合不变。
- `CountedNoAdd` → `testAddAllDelegation`：addAll 逐元素委托 add、空参数零调用。
- `testArrayStoreException` — Integer × String[] → ASE。
- `testEqualsIsIdentityByDefault` — 骨架保持恒等 equals。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| 默认 add（未覆写） | UnsupportedOperationException |
| 迭代器无 remove 时 remove/clear/removeIf | UnsupportedOperationException 透传 |
| toArray 组件类型不符 | ArrayStoreException |
| addAll/containsAll/removeAll/retainAll(null) | NullPointerException |

### JDK-8 vs JDK-27 差异（本类）
- 行为两侧实测一致（含 clear 逐元素经 iterator.remove 的调用模式）。
