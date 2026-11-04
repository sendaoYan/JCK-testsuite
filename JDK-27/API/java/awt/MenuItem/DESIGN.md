# java.awt.MenuItem 黑盒行为测试设计

## ① API 功能分组
- 构造：`MenuItem()`、`MenuItem(String label)`、`MenuItem(String, MenuShortcut)`。
- 标签/使能：`getLabel/setLabel`、`isEnabled/setEnabled/enable/disable`。
- 快捷键：`getShortcut/setShortcut/deleteShortcut`。
- 命令/事件：`getActionCommand/setActionCommand`、`addActionListener/removeActionListener/getActionListeners`。

## ② Java 用户用法
```java
MenuItem mi = new MenuItem("Open", new MenuShortcut(KeyEvent.VK_O));
mi.setEnabled(false);
mi.addActionListener(e -> {});
menu.add(mi);
```
组合链路：构造（含快捷键）→ 标签/使能 setter → 监听器 → 添加到 Menu。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `MenuItem` 默认 label=""、enabled=true（`MenuItem.java`）。
- `MenuItem.setShortcut` 存储 MenuShortcut（`MenuItem.java`）。
- `MenuItem.deleteShortcut` 置空 shortcut（`MenuItem.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认标签/使能 → `testBlackBox`。
- setLabel/setEnabled/enable/disable 往返 → `testBlackBox`。
- MenuShortcut getKey/usesShiftModifier + setShortcut/deleteShortcut → `testBlackBox`。
- setActionCommand + ActionListener 管理 → `testBlackBox`。
