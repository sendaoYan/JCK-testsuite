# javax.swing.RowFilter 设计案

## 1. API 功能
`RowFilter` 抽象类过滤表格行。方法分组：
- 工厂：`regexFilter/numberFilter/dateFilter/notFilter/andFilter/orFilter`。
- 判定（抽象）：`include(Entry)`。

## 2. Java 用户如何使用
用户创建过滤器并设置到 `TableRowSorter`：
```java
RowFilter<Object, Object> filter = RowFilter.regexFilter("^a");
sorter.setRowFilter(filter);
```
典型链路：`RowFilter.regexFilter` → `include`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/RowFilter.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/RowFilter.java`）。
- `TableRowSorter` 使用 `RowFilter` 过滤视图行。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖静态工厂 `regexFilter/numberFilter/notFilter/andFilter/orFilter` 及匿名 `include`。
