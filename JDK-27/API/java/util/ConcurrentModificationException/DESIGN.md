# java.util.ConcurrentModificationException — 测试设计案

## 1. API 功能
集合在遍历时被结构性修改（fail-fast）时抛出的运行时异常。JDK-27 有 4 个构造器：()、String、Throwable、String+Throwable。

## 2. Java 用户如何使用
在增强 for 中删除元素、或单线程迭代期间 add/remove 会触发；
也作为“检测到并发修改”的信号被用户代码 catch 后降级处理。
链路：iterator/forEach -> 结构性修改 -> next/checkForComodification -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `ArrayList.Itr.checkForComodification()`（本地 `~/git/jdk25u-dev`）在 modCount 变化后抛
  `new ConcurrentModificationException()`——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/ConcurrentModificationException/`。

## 4. 测试用例设计 + 自动化实现（ConcurrentModificationExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造器 | 4 个构造器全部存在且可用 |
| 消息/起因 | getMessage 回传字符串、getCause 回传 Throwable |
| fail-fast 触发 | 迭代中 add 后 next 抛 CME |
| P0 | 契约深化：构造矩阵/cause 保持/触发（fail-fast 迭代）；序列化 round-trip/initCause 一次性/suppressed/多载体触发 | testCmeContracts |

共 3 组场景、约 8 个 check（`testBlackBox()`）。
