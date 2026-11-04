# java.util.Stack 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。push/pop LIFO、peek 非破坏、empty、search（1-based 距栈顶、
> equals 判等非 ==、重复元素取最近者、缺失 -1、null 元素）、空栈 pop/peek 抛 EmptyStackException、
> 继承 Vector 的自底向顶迭代顺序、null 元素允许、clone 浅拷贝、序列化 round-trip +
> serialVersionUID、状态不变量。

## 1. API 提供了哪些功能

- **核心（5 方法）**：`push(E)`（返回压入元素）、`pop()`、`peek()`、`empty()`、`search(Object)`。
- **继承 Vector**：`add/get/set/remove`、`size/isEmpty/clear`、`capacity()`、`iterator()`（自底向顶）。
- **契约继承**：extends `Vector` → `Cloneable`/`Serializable`（`serialVersionUID == 1224463164541339165L`）。

## 2. Java 用户如何使用

- `new Stack<>()` → `push` 压栈 → `peek` 看顶 → `search` 定位 → `pop` 依次弹出（LIFO）→
  空栈 `pop/peek` 抛 `EmptyStackException`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Stack.html`：search 1-based、equals 判等、LIFO。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Stack.java`（search 实现基于
  `Vector.lastIndexOf`，因此 equals 判等且 null 安全）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/Stack`。

## 4. 测试用例设计（映射到 StackTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：push/pop、peek/search | testPushPop、testPeekSearch |
| P0 | search equals 判等 + 重复 + null 元素 | testSearchSemantics |
| P0 | null 元素允许 | testNullAllowance |
| P0 | 继承 Vector 迭代顺序/索引 | testInheritedVector |
| P1 | clone 浅拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `search(Object)` 基于 `Vector.lastIndexOf`：**equals 判等（非 ==）**，重复元素返回离栈顶最近者，
  缺失返回 -1；对 null 元素 `search(null)` 返回其 1-based 位置（null 安全）。
- 空栈 `pop()/peek()` 抛 `EmptyStackException`。
- 允许 null 元素（继承 Vector 语义）：`push(null)` 返回 null、`pop()` 返回被压入的 null。
- 继承 Vector 的迭代器自底向顶（栈底第一个 push 的先迭代）。
- `push(E)` 返回被压入的元素（非 void）。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 深化批次 II（契约级追加）

- **push 引用同一性**：`push(o) == o`；完整 LIFO 排干序列（c,b,a）；空 pop/peek→EmptyStackException；排干后可重灌。
- **equals/hashCode 走 List 公式**：[a]=128、[a,b]=4066；与 ArrayList 同内容互相 equals。
- **Vector legacy 继承**：addElement≡push、insertElementAt 底部不影响 peek、firstElement=栈底/lastElement=栈顶、
  setElementAt(顶) 改变 peek、底部 add(0,e) 后 pop 仍取最后元素。
- **subList=底部起的段**：set/remove 透传且不影响栈顶；Iterator CME（push 后）；iterator.remove 删栈底。
- **clone 浅拷贝**：元素共享（StringBuilder 突变互见）。
- **JDK-27 追加**：SequencedCollection 继承语义——getFirst=栈底、getLast=栈顶、addLast≡push、
  removeLast≡pop、reversed()=顶到底 List；空表 getLast→NSEE。
