# java.awt.MenuBar 黑盒行为测试设计

## ① API 功能分组
- 构造：`MenuBar()`。
- 菜单管理：`add(Menu)/remove(int)/remove(MenuComponent)/getMenuCount/getMenu`。
- 帮助菜单：`getHelpMenu/setHelpMenu`。
- 快捷键：`getShortcutMenuItem/deleteShortcut/shortcuts`。

## ② Java 用户用法
```java
MenuBar mb = new MenuBar();
Menu file = new Menu("File");
file.add(new MenuItem("Open", new MenuShortcut('O')));
mb.add(file);
mb.setHelpMenu(new Menu("Help"));
frame.setMenuBar(mb);
```
组合链路：构造 → add 菜单 → setHelpMenu → 快捷键查找/枚举 → remove。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `MenuBar.add(Menu)` 追加菜单并设置其父（`MenuBar.java`）。
- `MenuBar.getShortcutMenuItem` 遍历各 Menu 查找匹配快捷键的 MenuItem（`MenuBar.java`）。
- `MenuBar.deleteShortcut` 从所有菜单删除指定快捷键（`MenuBar.java`）。

## ④ 测试用例设计 + 自动化实现
- 初始空/无帮助菜单 → `testBlackBox`。
- add/getMenuCount/getMenu/setHelpMenu 往返 → `testBlackBox`。
- getShortcutMenuItem/deleteShortcut/shortcuts 枚举 → `testBlackBox`。
- remove(int)/remove(MenuComponent) → `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- `add((Menu) null)` 抛精确 NullPointerException；add 返回同一 Menu 实例。
- `getMenu(99)`/`remove(99)`/`remove(-1)` 抛精确 AIOOBE；remove(int) 后续菜单前移；getMenuCount 与 countItems 别名对 countMenus。
- help 菜单替换语义：第 2 个 setHelpMenu 替换第 1 个（不叠加）；help 计入 count；`setHelpMenu(null)` 移除 help（count 减回）。
- 快捷键注册表：getShortcutMenuItem 按 equals 匹配（不是 identity）；未注册返回 null；`shortcuts()` 枚举含已注册键；deleteShortcut 注销；shift 变体与 plain 是不同键（MenuShortcut 的 usesShiftModifier 参与 equals）。
