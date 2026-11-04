# java.awt.Choice 行为测试设计

## 1. API 功能
项管理：`add/addItem/insert/remove/removeAll/getItem/getItemCount`；选择：`select(int)/select(String)/getSelectedItem/getSelectedIndex/getSelectedObjects`；事件：ItemListener 增删查。

## 2. Java 用户如何使用
```java
Choice c = new Choice();
c.add("one"); c.add("two");
c.select(1);
```
构造 → 加项 → 选择 → 反例矩阵。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Choice.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractChoice()`）。
对应 `ChoiceTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- 空态契约：count 0、selectedIndex -1、selectedItem null。
- `add(null)`/`addItem(null)` 抛精确 NullPointerException；`add(String)` 被接受。
- 首个 add 自动把选中态置为 index 0（后续 add 不改）。
- `getItem(99)`/`getItem(-1)` AIOOBE；`insert(x,-1)` IAE；insert 在 count 处或越界均追加（不抛）；insert(0) 移位。
- `remove(99)`/`remove(-1)` AIOOBE；`remove(String 不存在或 null)` 抛 IAE（不是 NoSuchElementException）。
- `select(99)`/`select(-1)` IAE 且失败后保持原选择；`select(String 不存在/null)` 静默 no-op。
- removeAll 清空 items 且清空 selection（index 回 -1）；ItemListener 登记/移除对称。
