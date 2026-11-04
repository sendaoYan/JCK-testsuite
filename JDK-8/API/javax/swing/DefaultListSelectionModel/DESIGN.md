# javax.swing.DefaultListSelectionModel 测试设计

## 1. API 功能
ListSelectionModel 默认实现（Cloneable+Serializable）：BitSet 驱动的多区间选择、
anchor/lead 状态机、setValueIsAdjusting 手势合并、insertIndexInterval/removeIndexInterval 位移。

## 2. Java 用户如何使用
JList/JTable 内部持有；程序侧 `model.setSelectionInterval(0, 2); model.addSelectionInterval(5, 6);`
鼠标按下/拖动/释放期间 setValueIsAdjusting(true/false) 合并事件。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultListSelectionModel.java`；
JList.java/JTable.java；`test/jdk/javax/swing/JList/`。

## 4. 测试用例设计 + 易错细节
- 默认：空选择、MULTIPLE_INTERVAL、anchor==lead==-1、adjusting false。
- 区间代数：set 替换、add 并集、remove 差集；SINGLE 模式 set(2,9) 只选 2；
  SINGLE_INTERVAL set 不连续段钳到首段。
- **setSelectionMode(99) 抛 IllegalArgumentException "invalid selectionMode"**（与
  DefaultTreeSelectionModel 的钳制行为相反——两个模型对同一模式值行为不同，均锁定）。
- insertIndexInterval(3,2,true) 在 {3..5} 上位移出 {3..7}；removeIndexInterval 端点语义。
- `setSelectionInterval(-5,3)` 抛 IndexOutOfBoundsException（BitSet 层），但同参查询
  isSelectedIndex(-5) 返回 false 不抛——写抛读不抛的对照断言。
- lead 手势：setLeadSelectionIndex 过程中反选区间/保留外区间行为、getAnchorSelectionIndex 不动。
- 监听器：区间事件 first/last 精确；setValueIsAdjusting 合并期间照常投递、手势结束再投一次。
- clone 独立；序列化 round-trip 后选择保留。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
