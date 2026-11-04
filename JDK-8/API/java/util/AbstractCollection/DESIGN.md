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
