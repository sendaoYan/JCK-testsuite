# java.util.Set 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。add 重复返回 false、无重复元素、contains/remove、addAll 并集、
> removeAll 差集、retainAll 交集、containsAll、iterator、toArray、clear。

## 1. API 提供了哪些功能

- **核心**：`add`（重复返回 false）、`remove`、`contains`、`size/isEmpty/clear`。
- **批量**：`addAll`（并集）、`removeAll`（差集）、`retainAll`（交集）、`containsAll`。
- **视图/迭代**：`iterator`、`toArray()`、`toArray(T[])`。

## 2. Java 用户如何使用

- `set.add(x)` 天然去重（已存在返回 false）；`set.addAll(other)` 求并集；
  `set.removeAll(other)` 求差集；`set.retainAll(other)` 求交集。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Set.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Set.java`。

## 4. 测试用例设计（映射到 SetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | add 重复/contains/remove/clear | testBasics |
| P0 | addAll/removeAll/retainAll/containsAll | testBulk |
| P0 | iterator/toArray/clear | testIteration |

## 关键规范细节（易错，已锁入断言）

- `add` 重复元素返回 **false** 且集合不变；`remove` 不存在返回 **false**。
- `addAll` 有任一元素新增返回 true，全部已存在返回 false（同理 removeAll/retainAll）。

## 注意事项

- 用 HashSet（允许 null，无序）为载体；断言**不锁定迭代顺序**。
