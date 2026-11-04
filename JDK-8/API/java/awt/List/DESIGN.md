# java.awt.List 行为测试设计

## 1. API 功能
构造：`List()`/`(int rows)`/`(int rows,boolean multipleMode)`；项管理：`add/addItem(String,int)`、`replaceItem`、`remove(int/String)`、`removeAll`、`getItem(s)/getItemCount`；选择：`select/deselect/getSelectedIndex(es)/getSelectedItem(s)/isMultipleMode/setMultipleMode/makeVisible/getVisibleIndex`；事件：Item/ActionListener；rows 缺省 4。

## 2. Java 用户如何使用
```java
List l = new List(4, true);
l.add("a"); l.add("b");
l.select(1);
```
构造 → 顺序插入矩阵 → 单/多选语义 → 越界反例。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/List.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractList()`）。
对应 `ListTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `List(0)` 行数回退 4（负数 rows 同样接受）；`List(6)` 单选、`List(2,true)` 多选。
- `add(null)` 接受（与 Choice 相反）；`add(item,-1)` 与越界 index 均按追加处理；`add(item,1)` 精确插入；`replaceItem(null,0)` 接受。
- 越界：`replaceItem(x,99)`/`getItem(99)`/`remove(99)`/`remove(-2)` 均精确 AIOOBE；`remove("不存在")` 抛 IAE。
- 越界 select 的原始索引泄漏（历史怪癖锁定）：`select(-1)` 后 getSelectedIndex()==-1、`select(99)` 后 getSelectedIndex()==99（原样存储越界值），两种状态下 `getSelectedItems()` 都抛 AIOOBE；deselect 越界不修正索引；`makeVisible(-1)` no-op。
- 单选 reselect 迁移选择、deselect 清空；`setMultipleMode(false)` 保留已有多选但 getSelectedIndex() 报 -1。
