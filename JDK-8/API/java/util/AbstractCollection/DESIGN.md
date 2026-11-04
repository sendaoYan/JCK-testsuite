# java.util.AbstractCollection 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。默认 `add()` 抛 UOE（未覆写时）、`isEmpty()`（基于 size）、
> `contains`/`containsAll`（含 null）、`remove(Object)`（iterator.remove，含 null）、
> 批量 `addAll/removeAll/retainAll/clear`、`toArray()`/`toArray(T[])`（大小适配 / pad 尾部 null）、
> `toString()`（`[a, b, c]`）、null 参数 NPE。

## 1. API 提供了哪些功能

- **抽象方法（子类必须实现）**：`iterator()`、`size()`。
- **默认实现**：`isEmpty`、`contains`、`containsAll`、`add`（UOE）、`remove(Object)`、`addAll`、
  `removeAll`、`retainAll`、`clear`、`toArray`（两种）、`toString`。

## 2. Java 用户如何使用

- 自定义集合只需实现 `iterator()` + `size()`，其余方法继承自 AbstractCollection。
- 只读集合不覆写 `add` → 调用 `add` 抛 UOE。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/AbstractCollection.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/AbstractCollection.java`
  （默认 `add` 抛 UOE；`addAll/containsAll/removeAll/retainAll` 均 `Objects.requireNonNull(c)`）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/AbstractCollection`。

## 4. 测试用例设计（映射到 AbstractCollectionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认 add 抛 UOE + isEmpty | testDefaultAddUOE |
| P0 | contains/remove（含 null） | testContainsRemove |
| P0 | addAll/removeAll/retainAll/clear | testBulk |
| P0 | null 参数 NPE | testNullArgs |
| P0 | toArray()/toArray(T[]) 契约 | testToArray |
| P0 | toString 格式 | testToString |

## 关键规范细节（易错，已锁入断言）

- 默认 `add(E)` 抛 **UnsupportedOperationException**（未覆写时）；要测批量操作需子类覆写 `add`。
- `remove(Object)` 默认用 `iterator().remove()`，找不到返回 false；null 元素用 `==`/equals 语义。
- `toArray(T[])`：传入数组够大则复用并**在 size 位置置 null**；太小则新建；`toArray(null)` 抛 NPE。
- `addAll/containsAll/removeAll/retainAll(null)` 均抛 **NPE**（`Objects.requireNonNull(c)`）。

## 注意事项

- 无 GC/图形/网络/文件依赖；不依赖迭代顺序（用 contains 而非位置断言）。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法与探针
- `Rec`（记录型探针，仅 iterator()+size()，带 iterator 创建数/iterator.remove 计数）→
  `testDefaultDispatchRecording`：contains 扫描迭代器；remove(Object) 委托 iterator.remove
  （计数=1，miss 不调用）；removeAll/retainAll 的删除全部经 iterator.remove；
  clear 逐元素经 iterator.remove（计数==size）；contains(null)/remove(null) 走 == 分支；
  toString 对 null 渲染 "[x, null]"；removeIf 默认实现驱动 iterator.remove。
- `Fixed`（迭代器 remove 抛 UOE 的只读探针）→ `testUoePropagationFromIterator`：
  remove/clear/removeIf **透传底层 UOE 且集合不变**，查询不受影响。
- `CountedNoAdd`（add 恒 false 计数探针）→ `testAddAllDelegation`：addAll 逐元素委托 add
  （计数==参数长度）、恒 false 时 addAll 返回 false、空参数零调用。
- `testArrayStoreException` — Integer 元素 × String[] → ASE（store 时抛）。
- `testEqualsIsIdentityByDefault` — 骨架不覆写 equals：同内容两实例不等、自反成立。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| 默认 add（未覆写） | UnsupportedOperationException |
| 迭代器不支持 remove 时 remove/clear/removeIf | UnsupportedOperationException 透传 |
| toArray 组件类型不符 | ArrayStoreException |
| addAll/containsAll/removeAll/retainAll(null) | NullPointerException |
