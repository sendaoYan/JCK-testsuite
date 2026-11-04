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
