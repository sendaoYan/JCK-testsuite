# java.awt.CheckboxMenuItem 行为测试设计

## 1. API 功能
构造：`CheckboxMenuItem()`/`(String)`/`(String,boolean)`；状态：`getState/setState`、`getSelectedObjects`；事件：ItemListener 增删查；继承 MenuItem 的 setLabel/setEnabled/setShortcut/actionCommand。

## 2. Java 用户如何使用
```java
CheckboxMenuItem mi = new CheckboxMenuItem("grid", true);
mi.addItemListener(e -> {});
menu.add(mi);
```
构造 → 状态切换 → 继承能力走查。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/CheckboxMenuItem.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractCheckboxMenuItem()`）。
对应 `CheckboxMenuItemTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `CheckboxMenuItem(null)` 保留 null label 且 state=false；setState 幂等。
- 程序化 setState 不派发 ItemEvent；getSelectedObjects on 返回自身/off 返回 null。
- toString 精确携带 `state=true`/`state=false`（`java.awt.CheckboxMenuItem[...,state=...]`）。
- 继承 actionCommand 默认回退 label 语义；setShortcut/deleteShortcut 继承链路可用。
