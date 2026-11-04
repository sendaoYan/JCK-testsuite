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
