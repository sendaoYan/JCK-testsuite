# javax.swing.SortOrder 设计案

## 1. API 功能
`SortOrder` 枚举描述排序方向。方法分组：
- 枚举常量：`ASCENDING/DESCENDING/UNSORTED`。
- 工具：`values/valueOf`。

## 2. Java 用户如何使用
用户用枚举指定排序方向：
```java
sorter.setSortKeys(List.of(new RowSorter.SortKey(column, SortOrder.ASCENDING)));
SortOrder order = SortOrder.valueOf("DESCENDING");
```
典型链路：`SortOrder.ASCENDING` → `valueOf/values`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SortOrder.java` 定义该枚举（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SortOrder.java`）。
- `DefaultRowSorter`/`TableRowSorter` 使用该枚举。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验枚举及 3 个常量顺序。
- 行为（黑盒）：`testBlackBox()` 覆盖 `values/valueOf/ordinal/name`。
