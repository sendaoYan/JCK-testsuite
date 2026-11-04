# java.awt.MouseInfo 黑盒行为测试设计

## 1. API 功能
- 静态：`getPointerInfo()`（返回 `PointerInfo`，headless 抛 `HeadlessException`）、`getNumberOfButtons()`。

## 2. Java 用户如何使用
查询鼠标指针当前位置/设备与鼠标按键数：
```java
PointerInfo pi = MouseInfo.getPointerInfo();
Point p = pi.getLocation();
int buttons = MouseInfo.getNumberOfButtons();
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/MouseInfo.java`：委托 Toolkit 实现，headless 抛 `HeadlessException`。
- `test/jdk/java/awt/MouseInfo/`、`test/jdk/java/awt/Mouse/` 用 getPointerInfo/getNumberOfButtons 断言。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `getPointerInfo()` 返回 null（可接受）或非空时 location/device 非空。
- `getNumberOfButtons() >= -1`（文档域）。
headful。
