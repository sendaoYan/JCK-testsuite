# java.util.AbstractQueue — 测试设计案

## 1. API 功能
`Queue` 的最小骨架：要求实现 `offer/poll/peek/iterator/size`，并提供
`add/element/remove/clear/addAll` 的适配语义（add→offer、element→peek、remove→poll）。

- 抽象：`offer/poll/peek/iterator/size`（继承 AbstractCollection 的 iterator/size）。
- 骨架：`add(E)`（offer 失败抛 IllegalStateException）、`element()`（peek null 抛
  NoSuchElementException）、`remove()`（poll null 抛 NoSuchElementException）、`clear`（循环 poll）、
  `addAll`（逐个 add）。

## 2. Java 用户如何使用
自定义队列实现 offer/poll/peek 即得到完整 Queue 契约：add/element/remove 的“异常版”与
offer/peek/poll 的“返回哨兵版”。链路：new 子类 -> add -> element/remove -> poll/peek ->
空队列 element/remove 抛 NSEE -> clear -> addAll。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/AbstractQueue.java`（本地 `~/git/jdk25u-dev`）：
  `add` 在 `!offer(e)` 时抛 IllegalStateException；`element/remove` 在 peek/poll 为 null 时抛
  NoSuchElementException；`clear` 用 `while (poll() != null)`；`addAll` 逐个 add——第 1/2/4 节对齐。
- OpenJDK `src/java.base/share/classes/java/util/PriorityQueue.java` 继承 AbstractQueue。

## 4. 测试用例设计 + 自动化实现（AbstractQueueTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| add/element/remove | add 追加、element 只读队首、remove/poll 删除并返回 |
| 空队列异常 | element()/remove() 抛 NoSuchElementException；peek/poll 返回 null |
| clear | 清空 |
| addAll | 返回 true、按序添加 |
| 集成 | toString 经 iterator 输出 "[m, n]" |

共 5 组场景、约 12 个 check（`testBlackBox()`）。

## 契约深化追加（第二版）

新增 test 方法：testDispatchCounters / testAddFullExactMessage / testClearPollLoop /
testAddAllSemantics / testAddAllPartialFailure / testInheritedCollectionOps /
testEqualsHashCodeIdentity。

### 覆盖点

与 JDK-8 侧完全同构（行为双侧实测一致）：

- 分派计数器：add→1 次 offer、element→1 次 peek、remove→1 次 poll（1:1 累计）。
- add 满抛 ISE，message 恰为 **"Queue full"**。
- clear 为 poll 循环：2 元素 clear 恰 poll 3 次（n+哨兵）。
- addAll：空集合 false 零 offer；逐元素经 add；addAll(self) IAE；null 元素**不预检**——
  NPE 由底层队列中继、null 前元素已入队（NullRejecting 载体 + 部分应用断言）；
  有界 addAll 部分失败保留先前元素。
- 继承 contains/remove(Object)/containsAll/toString；身份 equals/hashCode。

### 易错规范细节

- JDK-27 与 JDK-8 在 AbstractQueue 公开行为上无差异（javap 双侧比对确认方法集相同）。
