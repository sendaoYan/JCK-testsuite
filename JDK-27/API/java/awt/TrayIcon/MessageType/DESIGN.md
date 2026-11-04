# java.awt.TrayIcon.MessageType 黑盒行为测试设计

## 1. API 功能
- `enum MessageType`：`ERROR`、`WARNING`、`INFO`、`NONE`（托盘消息类型）。
- 枚举 API：`values()`、`valueOf(String)`、`ordinal()`、`name()`。

## 2. Java 用户如何使用
`TrayIcon.displayMessage(caption, text, MessageType.INFO)` 弹托盘通知；`MessageType.NONE` 表示无图标。
```java
trayIcon.displayMessage("提示", "已完成", TrayIcon.MessageType.INFO);
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/TrayIcon.java`：`MessageType` 为 `TrayIcon` 内部枚举，`displayMessage` 第三个参数用它选平台图标。
- `test/jdk/java/awt/TrayIcon/` 用 `displayMessage` 传 `MessageType` 做托盘通知测试。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `values().length==4`、声明顺序 ordinal 0..3。
- `valueOf(name)` 与 `name()` 往返、四常量互异。
- `valueOf("BOGUS")` 抛 `IllegalArgumentException`。
纯枚举，headless-safe。
