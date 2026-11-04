# javax.swing.ListSelectionModel 测试设计

## 1. API 功能
列表选择契约接口：三模式常量（SINGLE_SELECTION=0/SINGLE_INTERVAL_SELECTION=1/
MULTIPLE_INTERVAL_SELECTION=2）、区间操作 set/add/removeSelectionInterval、
anchor/lead、insertIndexInterval/removeIndexInterval、getValueIsAdjusting、监听器注册。

## 2. Java 用户如何使用
`list.getSelectionModel().setSelectionMode(SINGLE_SELECTION);` JTable 行列选择各持一个；
监听 ListSelectionEvent 的 getFirstIndex/getLastIndex 做增量刷新。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ListSelectionModel.java`；
JList/JTable/DefaultTableColumnModel 调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、20 个经典方法全部 abstract；常量精确值 0/1/2。
- **removeIndexInterval(index0,index1) 接收两个端点（区间）而非 index+length**；
  insertIndexInterval(index,length,before) 才是 index+length——两个签名语义对照断言。
- 多态：DefaultListSelectionModel 经接口引用驱动区间并/差/插入位移/手势标志/监听器投递。
- 版本差异（javap/实测确认）：JDK-21+ 新增 default 方法 getSelectedIndices():int[] 与
  getSelectedItemsCount():int（JDK-8 接口均无）。JDK-27 侧断言 22 个方法名 + default 形态 +
  行为（[4,5,6]/计数 3/清空后 0）；JDK-8 侧断言 20 个方法且无 default 方法。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
