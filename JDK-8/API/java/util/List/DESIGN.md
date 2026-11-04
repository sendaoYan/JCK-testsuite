# java.util.List 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。get/set/add(index)/remove(index)、indexOf/lastIndexOf、subList、
> listIterator 导航、Java 8 默认方法 sort（含 sort(null) 自然序）/replaceAll/removeIf。

## 1. API 提供了哪些功能

- **核心**：`get/set/add/remove`（按索引）、`indexOf/lastIndexOf`、`subList`、`listIterator`。
- **默认方法（Java 8）**：`sort`、`replaceAll`、`removeIf`（Collection）。

## 2. Java 用户如何使用

- `list.sort(cmp)` 排序（`sort(null)` 走自然序）；`list.replaceAll(fn)` 原地变换；`list.removeIf(pred)` 过滤。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/List.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/List.java`。

## 4. 测试用例设计（映射到 ListTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 索引操作 + indexOf/lastIndexOf | testIndexedOps |
| P0 | subList 视图 | testSubList |
| P0 | listIterator 导航 | testListIterator |
| P0 | sort（含 sort(null) 自然序） | testSort |
| P0 | replaceAll/removeIf/null NPE | testReplaceAllRemoveIf |

## 关键规范细节（易错，已锁入断言）

- `sort(null)` 使用**自然序**（元素须 Comparable），不抛 NPE。
- `replaceAll(null)` 抛 **NPE**。

## 注意事项

- 无 GC/图形/网络/文件依赖；用 ArrayList（有序）为载体断言顺序。
