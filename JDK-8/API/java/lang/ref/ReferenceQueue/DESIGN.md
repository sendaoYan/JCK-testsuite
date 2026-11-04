# java.lang.ref.ReferenceQueue 测试设计案（JDK-8）

> 覆盖目标：完整契约。poll 空→null、remove(1) 空→null、remove(-1)/MIN_VALUE→IAE、enqueue→poll/remove、
> remove() 已入队引用、remove(0) 阻塞语义（不在断言内）。

## 1. API 功能

- `poll()` 非阻塞取队首；`remove()`/`remove(long)` 阻塞/超时取引用。

## 2. 用户如何使用

- 配合 Reference 的 enqueue 观察回收事件；`poll()` 非阻塞，`remove(timeout)` 阻塞至超时。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/ReferenceQueue.html）。
- JCK runtime-8b `tests/api/java_lang/ref/ReferenceQueue/`。

## 4. 测试用例设计（映射到 ReferenceQueueTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 空队列 poll/remove(1)/remove(-1) | testEmptyQueue |
| P0 | enqueue→poll/remove 往返 | testPollAndRemove |

## 易错规范细节（已锁入断言）

- `remove(-1)`/`remove(MIN_VALUE)` → IllegalArgumentException（JDK-8）。
