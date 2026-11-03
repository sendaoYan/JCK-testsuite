# java.awt.SystemTray 黑盒行为测试设计

## 1. API 功能
- 静态：`isSupported()`、`getSystemTray()`。
- 实例：`add/remove(TrayIcon)`、`getTrayIcons()`、`getTrayIconSize()`、属性变更监听器（`add/remove/getPropertyChangeListeners`）。

## 2. Java 用户如何使用
管理系统托盘图标：
```java
if (SystemTray.isSupported()) {
    SystemTray tray = SystemTray.getSystemTray();
    tray.add(trayIcon);
}
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/SystemTray.java`：`getSystemTray` 在 headless 抛 `HeadlessException`、不支持时抛 `UnsupportedOperationException`；`add` 对已添加图标抛 `IllegalArgumentException`、托盘缺失抛 `AWTException`。
- `test/jdk/java/awt/TrayIcon/`、`test/jdk/java/awt/SystemTray/` 添加/移除托盘图标并监听属性。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，健壮模式）：
- `isSupported()` 返回布尔。
- 支持：`getSystemTray` 非空且单例；`getTrayIcons` 非空数组；null 监听器 add/remove 为 no-op；`getPropertyChangeListeners` 非空。
- 不支持：`getSystemTray` 抛 `UnsupportedOperationException`。
headful。
