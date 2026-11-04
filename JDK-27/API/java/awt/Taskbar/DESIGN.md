# java.awt.Taskbar 黑盒行为测试设计

## 1. API 功能
- 静态：`isTaskbarSupported()`、`getTaskbar()`。
- 实例：`isSupported(Feature)`、`requestUserAttention`、`setIconImage/getIconImage`、`setIconBadge`、`setProgressValue`、`setMenu/getMenu` 等。
- 嵌套枚举：`Taskbar.Feature`（10）、`Taskbar.State`（5）。

## 2. Java 用户如何使用
与桌面任务栏/程序坞集成（图标、徽标、进度、用户注意）：
```java
if (Taskbar.isTaskbarSupported()) {
    Taskbar tb = Taskbar.getTaskbar();
    if (tb.isSupported(Taskbar.Feature.ICON_IMAGE)) tb.setIconImage(image);
}
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Taskbar.java`：`getTaskbar` 在 headless 抛 `HeadlessException`、不支持时抛 `UnsupportedOperationException`；`isSupported(Feature)` 委托 peer。
- `test/jdk/java/awt/Taskbar/` 断言支持探测与图标/进度设置。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，健壮模式）：
- `isTaskbarSupported()` 返回布尔。
- 支持：`getTaskbar` 非空且单例；对全部 10 个 Feature `isSupported` 确定性；抽查 ICON_IMAGE/PROGRESS_VALUE。
- 不支持：`getTaskbar` 抛 `UnsupportedOperationException`。
不调用 setIconImage/setProgressValue 等（无桌面服务时不安全）。
