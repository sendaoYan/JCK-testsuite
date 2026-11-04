# java.awt.Desktop 黑盒行为测试设计

## 1. API 功能
- 静态：`isDesktopSupported()`、`getDesktop()`。
- 实例：`isSupported(Action)`、`open/edit/print(File)`、`browse/mail(URI)`、`mail()`、`moveToTrash(File)`、`requestForeground`、应用事件处理器设置等。
- 嵌套枚举：`Desktop.Action`（24 个动作）。

## 2. Java 用户如何使用
启动系统默认应用打开文件/URL/邮件：
```java
if (Desktop.isDesktopSupported()) {
    Desktop d = Desktop.getDesktop();
    if (d.isSupported(Desktop.Action.OPEN)) d.open(file);
}
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Desktop.java`：`getDesktop` 在 headless 抛 `HeadlessException`、不支持时抛 `UnsupportedOperationException`；`isSupported(Action)` 委托 peer。
- `src/java.desktop/share/classes/java/awt/desktop/`（AboutHandler 等接口）。
- `test/jdk/java/awt/Desktop/` 断言 isDesktopSupported/isSupported 与打开动作。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，健壮模式）：
- `isDesktopSupported()` 返回布尔。
- 支持：`getDesktop` 非空且单例；对全部 24 个 Action `isSupported` 确定性；抽查 OPEN/BROWSE/MAIL/APP_QUIT_HANDLER。
- 不支持：`getDesktop` 抛 `UnsupportedOperationException`。
不真正调用 open/browse/mail（避免拉起外部程序）。
