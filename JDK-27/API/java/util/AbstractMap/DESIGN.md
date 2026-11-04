# java.util.AbstractMap — 测试设计案

## 1. API 功能
`Map` 的最小骨架：只要求实现 `entrySet()`，其余（size/isEmpty/containsValue/containsKey/get/
remove/putAll/clear/keySet/values/equals/hashCode/toString/clone）基于 entrySet 提供；put/remove
默认抛 UnsupportedOperationException。

- 抽象：`entrySet()`。
- 骨架：size/isEmpty/containsValue/containsKey/get/put/remove/putAll/clear/keySet/values/
  equals/hashCode/toString/clone。

## 2. Java 用户如何使用
自定义 Map 只写 entrySet（+可选 put/remove 变可写）即获得全部 Map 能力；keySet/values 是惰性视图。
链路：new 子类 -> put -> get/containsKey/containsValue -> remove/putAll/clear -> keySet/values/
entrySet -> equals/hashCode -> toString -> clone。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/AbstractMap.java`（本地 `~/git/jdk25u-dev`）：
  `put/remove` 抛 UnsupportedOperationException；`toString` 拼 `{k1=v1, k2=v2}`；`equals` 基于
  entrySet 大小与 contains——第 4/5/6 节对齐；`clone` 是 protected 浅拷贝。
- OpenJDK `test/jdk/java/util/AbstractMap/Equals.java`、`ToString.java`、`AbstractMapClone.java`：
  官方测试分别覆盖 equals/toString/clone。
- OpenJDK `src/java.base/share/classes/java/util/HashMap.java` 继承 AbstractMap。

## 4. 测试用例设计 + 自动化实现（AbstractMapTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| put/get | put 返回旧值或 null、get、containsKey/Value、size/isEmpty |
| remove/putAll/clear | remove 返回并删除、putAll 批量、clear 清空 |
| 视图 | keySet/values/entrySet（序无关断言） |
| equals/hashCode | 序无关相等、不同 size/元素不等、null/异型 false |
| toString | "{k=1}" |
| 只读骨架 | 默认 put/remove 抛 UnsupportedOperationException |
| clone | protected clone 浅拷贝、副本独立（不共享底层存储） |

共 7 组场景、约 22 个 check（`testBlackBox()`）。
