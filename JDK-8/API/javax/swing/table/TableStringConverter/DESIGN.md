# javax.swing.table.TableStringConverter 测试设计

## 1. API 功能
抽象字符串包装器：`toString(TableModel, int row, int col)` 一个抽象方法，供 TableRowSorter
在比较前把单元格值转成可比较字符串（避免反射调用 toString 的开销）。

## 2. Java 用户如何使用
继承并实现 toString；`rowSorter.setStringConverter(conv)`；排序时比较走 conv 而非
value.toString()（如本地化数字格式）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/TableStringConverter.java`；
TableRowSorter 内部调用。

## 4. 测试用例设计 + 易错细节
- 反射形态：abstract 类、恰好 1 个抽象方法、Serializable。
- 行为：匿名子类返回固定字符串经 TableRowSorter 排序生效（大写映射改变顺序）。
- 版本差异（javap 确认）：JDK-8 无参构造器为 public，JDK-9+ 收紧为 protected——两侧分别断言。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
