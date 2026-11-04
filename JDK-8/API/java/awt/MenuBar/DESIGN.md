# java.awt.MenuBar 行为测试设计

## 1. API 功能
构造 `MenuBar()`；菜单管理：`add/remove(int|MenuComponent)/getMenu/getMenuCount/countMenus`；help 菜单：`setHelpMenu/getHelpMenu`；快捷键：`shortcuts()/getShortcutMenuItem/deleteShortcut`。

## 2. Java 用户如何使用
```java
MenuBar mb = new MenuBar();
mb.add(new Menu("File"));
mb.setHelpMenu(new Menu("Help"));
```
构造 → 菜单序列 → help 替换语义 → 快捷键注册表。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/MenuBar.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractMenuBar()`）。
对应 `MenuBarTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `add((Menu) null)` 抛精确 NullPointerException；add 返回同一 Menu 实例。
- `getMenu(99)`/`remove(99)`/`remove(-1)` 抛精确 AIOOBE；remove(int) 后续菜单前移；getMenuCount 与 countItems 别名对 countMenus。
- help 菜单替换语义：第 2 个 setHelpMenu 替换第 1 个（不叠加）；help 计入 count；`setHelpMenu(null)` 移除 help（count 减回）。
- 快捷键注册表：getShortcutMenuItem 按 equals 匹配（不是 identity）；未注册返回 null；`shortcuts()` 枚举含已注册键；deleteShortcut 注销；shift 变体与 plain 是不同键（MenuShortcut 的 usesShiftModifier 参与 equals）。
