# java.util.EmptyStackException — 测试设计案

## 1. API 功能
对空 `Stack` 执行 peek/pop 时抛出的运行时异常。仅一个无参构造器。

## 2. Java 用户如何使用
`Stack` 为空时调用 `pop()`/`peek()` 抛出，用户用它做空栈守卫。
链路：空 Stack -> pop/peek -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Stack.peek/pop`（本地 `~/git/jdk25u-dev`）为空时 `throw new EmptyStackException()`——
  第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/Stack/`。

## 4. 测试用例设计 + 自动化实现（EmptyStackExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造器 | 无参构造可用、消息 null |
| 触发 | 空 Stack pop 抛 EmptyStackException |
| 非空 | push 后可 pop 不抛 |
| P0 | 契约深化：null 消息/Stack 触发矩阵/层次；唯一构造器参数/序列化 round-trip/空栈 search 不抛/栈轨迹可设 | testEmptyStackContracts、testStackTraceContract |

共 3 组场景、约 4 个 check（`testBlackBox()`）。
