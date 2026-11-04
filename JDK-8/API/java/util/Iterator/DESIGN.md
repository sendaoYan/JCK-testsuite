# java.util.Iterator 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。hasNext/next、next 耗尽抛 NSEE、remove 移除最后返回元素、
> remove 无前置 next/连续 remove 抛 ISE、Java 8 默认 forEachRemaining。

## 1. API 提供了哪些功能

- **抽象方法**：`hasNext()`、`next()`。
- **默认方法（Java 8）**：`forEachRemaining(Consumer)`、`remove()`（默认抛 UOE）。

## 2. Java 用户如何使用

- `while (it.hasNext()) process(it.next())`；`it.forEachRemaining(consumer)` 消费剩余元素；
  需要删除时在 `next()` 后调用 `remove()`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Iterator.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/ArrayList.java`（Itr.remove 的 ISE）。

## 4. 测试用例设计（映射到 IteratorTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | next/remove/耗尽 NSEE | testNextRemove |
| P0 | forEachRemaining | testForEachRemaining |
| P0 | remove 无前置 next/连续 remove ISE | testRemoveWithoutNext |

## 关键规范细节（易错，已锁入断言）

- `next()` 耗尽后抛 **NoSuchElementException**。
- `remove()` 在无前置 `next()` 或已连续调用一次后抛 **IllegalStateException**（ArrayList 实现）。

## 注意事项

- 无 GC/图形/网络/文件依赖；用 ArrayList（有序）为载体断言顺序。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testIteratorDefaults` — Bare 探针（只实现 hasNext/next）：默认 remove → UOE（next 前后均）；
  默认 forEachRemaining 以 hasNext/next 逐个消费（顺序锁定）、耗尽后 no-op、
  **有剩余元素时 forEachRemaining(null) → NPE**、空迭代器 next → NSEE。
- `testHasNextStability` — hasNext 幂等（连续调用不推进状态、结果一致）。
- `testRemoveLifecycle` — 末次 next 后 remove 合法、连续第二次 remove ISE、
  删尽后 next NSEE、单元素删除后耗尽。
- `testConsumerExceptionRelay` — forEachRemaining 的 action 抛出的**原异常对象**透传
  （ArrayList 迭代器与接口默认实现两路径）。
- `testUnsupportedRemoveFlavors` — Arrays.asList（定长）迭代器 remove → UOE。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| remove() 无前置 next / 连续第二次 remove | IllegalStateException |
| next() 耗尽后 | NoSuchElementException |
| 默认 remove（实现未提供） | UnsupportedOperationException |
| forEachRemaining(null)（且有剩余） | NullPointerException |
| Arrays.asList 迭代器 remove | UnsupportedOperationException |
| action 抛出的 RuntimeException | 原样透传（非包装） |
