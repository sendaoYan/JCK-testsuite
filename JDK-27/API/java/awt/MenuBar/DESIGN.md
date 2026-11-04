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
