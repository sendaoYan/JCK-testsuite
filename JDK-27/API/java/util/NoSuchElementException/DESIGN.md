# java.util.NoSuchElementException — 测试设计案

## 1. API 功能
取用不存在元素时抛出的运行时异常（迭代耗尽、空栈/队列等）。JDK-27 有 4 个构造器：()、String、Throwable、String+Throwable。

## 2. Java 用户如何使用
`iterator.next()` 耗尽、`Stack.pop()` 空栈、`Optional.get()` 空值等都会抛出。
链路：集合为空/游标越界 -> next/pop/get -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK 各集合迭代器（`ArrayList.Itr.next()`）越界抛 `new NoSuchElementException()`；
  `Stack.peek/pop`、`ArrayDeque.removeFirst` 同理——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/NoSuchElementException/`。

## 4. 测试用例设计 + 自动化实现（NoSuchElementExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造器 | 4 个构造器全部存在且可用 |
| 消息/起因 | getMessage/getCause 回传 |
| 触发 | 空迭代器 next 抛 NSEE |
| P0 | 契约深化：触发矩阵/(Throwable) 构造(9+)/空迭代器触发；序列化 round-trip/直接构造 (Throwable)/(String,Throwable)/Vector 首末触发 | testNseeContracts |

共 3 组场景、约 8 个 check（`testBlackBox()`）。
