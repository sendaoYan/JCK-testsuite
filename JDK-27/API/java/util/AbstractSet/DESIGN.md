# java.util.AbstractSet — 测试设计案

## 1. API 功能
`Set` 的最小骨架：只要求 `iterator()` + `size()`，并额外提供 `equals`（size + containsAll，序无关）
与 `hashCode`（元素 hashCode 之和）。

- 抽象：`iterator()`、`size()`。
- 骨架覆写：`equals`、`hashCode`、`removeAll`（比 AbstractCollection 的迭代 remove 更优）。
- 继承：add/contains/remove/containsAll/toArray 等来自 AbstractCollection。

## 2. Java 用户如何使用
自定义 Set 只写 iterator+size 即获得 equals/hashCode 的集合语义（无序比较）；removeAll 交集删除。
链路：new 子类 -> add（去重） -> size/contains -> equals/hashCode（序无关） -> removeAll -> toArray。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/AbstractSet.java`（本地 `~/git/jdk25u-dev`）：
  `equals` = `size 相等 && containsAll(other)`；`hashCode` = 元素 hashCode 之和——第 2/3 节对齐；
  `removeAll` 在小集合上按元素 remove 优化。
- OpenJDK `src/java.base/share/classes/java/util/HashSet.java` 继承 AbstractSet。
- Guava 大量不可变 Set（`ImmutableSet`）继承/参照 AbstractSet 语义。

## 4. 测试用例设计 + 自动化实现（AbstractSetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 增删查 | add 去重返回 false、size/contains/isEmpty/remove |
| equals | 序无关相等、元素不同不等 |
| hashCode | 相等集合同 hashCode；数值集合 hashCode == 元素 hashCode 之和 |
| equals 边界 | null/异型 false、自反 true |
| removeAll | 返回 true 并删除交集 |
| toArray | 迭代序 |

共 6 组场景、约 14 个 check（`testBlackBox()`）。
