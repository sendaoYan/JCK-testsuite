# java.util.HashSet 测试设计案（JDK-27）

> 覆盖目标：**完整契约**，白盒（反射结构）+ 黑盒（行为契约）。构造器矩阵、null 元素语义、
> 迭代器写通道、fail-fast、toArray 契约、跨实现 equals、clone、序列化、spliterator、状态不变量全部补齐；
> 外加 JDK 19+ `HashSet.newHashSet(int)` 工厂。

## 1. API 提供了哪些功能

- **构造器（4）**：`HashSet()`、`HashSet(int)`、`HashSet(int,float)`、`HashSet(Collection)`。
- **JDK 19+ 工厂**：`HashSet.newHashSet(int)`。
- **核心操作**：`add/remove/contains/size/isEmpty/clear/addAll/removeAll/retainAll/removeIf`。
- **契约继承**：`clone()`、`Serializable`（`serialVersionUID == -5024744406713321676L`）、
  `spliterator()`（SIZED|DISTINCT）。

## 2. Java 用户如何使用

- 与 JDK-8 一致；JDK 19+ 可用 `newHashSet(n)` 预分配。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** `java/util/HashSet.html`（JDK-8 与 JDK-27）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/HashSet` + `~/git/jdk/test/jdk/java/util/HashSet`。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/HashSet`。

## 4. 测试用例设计（映射到 HashSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒结构：继承 AbstractSet、实现 Set/Cloneable/Serializable、成员形态 | testWhiteBox |
| P0 | 构造器矩阵 + null 语义 | testConstructorMatrix、testNullSemantics |
| P0 | 迭代器写通道 + fail-fast | testIteratorWriteChannels、testFailFast |
| P0 | 跨实现 equals | testCrossImplEquals |
| P1 | toArray、clone、序列化、spliterator、状态不变量 | testToArrayContract、testClone、testSerialization、testSpliterator、testStateInvariants |
| P1 | JDK 19+ newHashSet(int) 工厂 | testBlackBox（已含） |

## 关键规范细节（易错，已锁入断言）

- 同 JDK-8 版；额外：`newHashSet(int)` 是 JDK 19+ 静态工厂，预分配容量。

## 注意事项

- 白盒反射只断言公开结构；serialVersionUID 与 JDK-8 相同。
