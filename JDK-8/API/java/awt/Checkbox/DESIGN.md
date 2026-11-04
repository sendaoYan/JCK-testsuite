# java.awt.Checkbox 行为测试设计

## 1. API 功能
构造：`Checkbox()`/`(String)`/`(String,boolean)`/`(String,boolean,CheckboxGroup)`/`(String,CheckboxGroup,boolean)`；状态/标签：`getLabel/setLabel`、`getState/setState`、`getSelectedObjects`；组：`getCheckboxGroup/setCheckboxGroup`；事件：ItemListener 增删查。

## 2. Java 用户如何使用
```java
Checkbox a = new Checkbox("A", true, group);
a.addItemListener(e -> {});
```
构造 → 组语义 → 监听器 → add 到 Frame。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Checkbox.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractCheckbox()`）。
对应 `CheckboxTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `Checkbox(null)` 保留 null label；默认 ("", false, 无组)。
- 组选择迁移：`setState(true)` 把组选择迁到本成员（旧成员翻 false）；第 2 个 true 构造进组会抢走选择。
- 历史行为锁定：对组内当前选中成员 `setState(false)` 是彻底 no-op（自身 state 保持 true、组引用保持）——两个 JDK 实测一致。
- `setSelectedCheckbox(null)` 把成员 state 翻 false；detach（setCheckboxGroup(null)）保留自身 state 并清除组选择。
- `getSelectedObjects()` 选中时返回含自身引用的数组、未选中返回 null。
- 同一 ItemListener 注册两次计数 2；程序化 setState 不派发 ItemEvent。
