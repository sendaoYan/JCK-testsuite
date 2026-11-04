# java.awt.MenuItem 行为测试设计

## 1. API 功能
构造：`MenuItem()`/`(String)`/`(String,MenuShortcut)`；标签/使能：`getLabel/setLabel`、`isEnabled/setEnabled` 及 deprecated `enable()/disable()`；快捷键：`getShortcut/setShortcut/deleteShortcut`；命令与事件：actionCommand 回退、ActionListener 增删查。

## 2. Java 用户如何使用
```java
MenuItem mi = new MenuItem("Open", new MenuShortcut(KeyEvent.VK_O));
mi.addActionListener(e -> {});
```
构造 → deprecated 别名 → 快捷键 equals/hashCode → 命令回退。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/MenuItem.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractMenuItem()`）。
对应 `MenuItemTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `MenuItem(null)` 保留 null；`enable()`/`disable()` 是 setEnabled 的 deprecated 别名（行为等价）。
- actionCommand 默认回退 label、显式优先、`setActionCommand(null)` 回退 label（与 Button 同一三段链）。
- MenuShortcut 语义：getKey 返回虚拟键码、默认无 shift、equals 对称且 hashCode 一致、shift 变体与 plain 不等。
- 同一 ActionListener 重复注册计数 2；移除单个实例。
