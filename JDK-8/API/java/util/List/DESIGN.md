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

## 深化批次 II（契约级追加）

- **hashCode/equals 契约**：公式锁定（1/128/4066/126046）跨 ArrayList/LinkedList/Vector 一致；
  equals 元素序敏感、大小敏感、null 元素相等性。
- **sort 默认方法**：nullsFirst、comparing 键提取、reversed 链、**稳定性**（相等键保序）。
- **replaceAll 默认方法**：Arrays.asList set 透传可用；unmodifiableList→UOE；null 元素映射。
- **Arrays.asList 视图**：set 返回旧值透传；add/remove/clear→UOE；subList add→UOE 而 set 透传。
- **subList 视图契约**：equals/hashCode 按自身内容；set/add/clear 透传；全范围 subList equals 原表。
- **越界矩阵**：get/set/add/remove/addAll/listIterator 全部 (-1)/(size+1) IOOBE 且失败不留痕。
- **JDK-27 追加**：List.of 全矩阵（迭代序=参数序、hashCode 公式、全部写通道 UOE、null NPE、
  RandomAccess、subList 不可变、空表查询/越界）；List.copyOf（快照独立、不可变、null NPE、subList 源）；
  **List.ofLazy**（size() 不调生成函数、get 每下标恰一次并缓存、迭代、不可变、null fn NPE、负 size IAE、
  RandomAccess）；排序稳定性与比较器链。
