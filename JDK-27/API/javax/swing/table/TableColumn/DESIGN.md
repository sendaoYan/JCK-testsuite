# javax.swing.table.TableColumn 测试设计

## 1. API 功能
JTable 列元数据：modelIndex（模型列索引，getHeaderValue/identifier/headerRenderer/cellRenderer/
cellEditor/minWidth/maxWidth/preferredWidth/resizable）、宽度状态机（width 在 [min,max] 内收敛）。

## 2. Java 用户如何使用
`TableColumn c = table.getColumnModel().getColumn(0); c.setPreferredWidth(80); c.setHeaderValue("Name");`
拖动列时 UI 改 width；程序调 setMinWidth/setMaxWidth 约束宽度。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/TableColumn.java`；
JTable/TableColumnModel 调用点；`test/jdk/javax/swing/table/JTableHeader/`。

## 4. 测试用例设计 + 易错细节
- 默认值：modelIndex 0 时 width=preferredWidth=75、min=15、max=Integer.MAX_VALUE、resizable=true、
  identifier==headerValue==null。
- 宽度收敛：setMinWidth(50) 抬升当前 width；setMaxWidth(40) 压低且 width 落到 max；
  setPreferredWidth 不改变 width；setWidth 越界时被夹到区间。
- setHeaderValue/setIdentifier 独立（identifier 为 null 时用 headerValue）。
- PropertyChangeListener：preferredWidth/headerValue/minWidth 变更投递精确属性名
  （"preferredWidth"/"headerValue"/"minWidth"），未变更的 set 同值不投递。
- 序列化 round-trip 后 modelIndex/宽度/headerValue 保留。
- 易错点：setWidth 与 setMinWidth/setMaxWidth 的次序影响最终 width；resizable=false 只是 UI 不拖。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
