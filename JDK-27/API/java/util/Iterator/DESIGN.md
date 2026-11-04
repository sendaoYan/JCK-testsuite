# java.util.Iterator — 测试设计案

## 1. API 功能
集合/流的通用游标接口：hasNext/next 遍历，remove（default，可抛 UnsupportedOperationException）、
forEachRemaining（default）。

- 抽象：`hasNext()`、`next()`。
- default：`remove()`（抛 UnsupportedOperationException）、`forEachRemaining(Consumer)`。

## 2. Java 用户如何使用
`Iterator<E> it = c.iterator(); while (it.hasNext()) { E e = it.next(); ... }`；增强 for 底层即
Iterator；`it.forEachRemaining(...)` 消费剩余。
链路：hasNext/next -> 耗尽 next 抛 NSEE -> remove 删除 -> forEachRemaining 消费剩余。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/Iterator.java`（本地 `~/git/jdk25u-dev`）：
  remove 默认抛 UnsupportedOperationException、forEachRemaining 循环 next——第 3/4/5 节对齐。
- OpenJDK `test/jdk/java/util/Iterator/IteratorDefaults.java` 等官方测试。
- 所有集合框架（ArrayList/HashSet…）的迭代都实现此接口。

## 4. 测试用例设计 + 自动化实现（IteratorTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 遍历 | hasNext/next 逐元素、耗尽 |
| 异常 | 耗尽后 next 抛 NoSuchElementException |
| remove | 删除最后返回元素 |
| forEachRemaining | 消费剩余 |
| 不可变 remove | 抛 UnsupportedOperationException |
| 空迭代器 | hasNext false、next 抛 NSEE |

共 6 组场景、约 12 个 check（`testBlackBox()`）。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `testIteratorDefaults` — Bare 探针：默认 remove UOE（next 前后均）；默认 forEachRemaining 逐元素
  顺序消费、耗尽 no-op、有剩余时传 null action → NPE。
- `testHasNextStability` — hasNext 幂等不推进。
- `testRemoveLifecycle` — 末次 next 后 remove 合法、二次 remove ISE、删尽后 next NSEE。
- `testConsumerExceptionRelay` — action 异常原对象透传（ArrayList 与接口默认两路径）。
- `testUnsupportedRemoveFlavors` — Arrays.asList 迭代器 remove UOE；空集合迭代器 next NSEE。

### 负例精确异常表
| 操作 | 异常 |
|---|---|
| remove() 无前置 next / 连续第二次 | IllegalStateException |
| next() 耗尽后 | NoSuchElementException |
| 默认 remove / Arrays.asList 迭代器 remove | UnsupportedOperationException |
| forEachRemaining(null)（有剩余） | NullPointerException |
| action 抛 RuntimeException | 原样透传 |

### JDK-8 vs JDK-27 差异（本类）
- 仅字节码层面 remove/forEachRemaining 在 27 显式 default（javap 显示差异）；行为两侧实测一致。
