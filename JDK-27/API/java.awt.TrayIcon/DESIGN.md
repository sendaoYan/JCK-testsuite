# java.awt.TrayIcon 黑盒行为测试设计

## 1. API 功能
- 构造：`TrayIcon(Image)`、`TrayIcon(Image,String)`、`TrayIcon(Image,String,PopupMenu)`。
- 属性：`set/getImage`、`set/getToolTip`、`set/getPopupMenu`、`set/getImageAutoSize`、`isImageAutoSize`、`set/getActionCommand`、`getSize`。
- 事件监听：鼠标/动作监听器；`displayMessage`（配 `MessageType` 枚举）。

## 2. Java 用户如何使用
创建托盘图标并配置提示、自动缩放、动作命令：
```java
TrayIcon ti = new TrayIcon(image, "tooltip");
ti.setImageAutoSize(true);
ti.displayMessage("标题", "内容", TrayIcon.MessageType.INFO);
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/TrayIcon.java`：所有构造器在系统托盘不支持时抛 `UnsupportedOperationException`（`SystemTray.isSupported()==false`）；`autosize` 默认 false；`getSize` 委托 `SystemTray.getTrayIconSize`。
- `test/jdk/java/awt/TrayIcon/` 构造图标、设 tooltip/autoSize、displayMessage。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，健壮模式）：
- 支持：构造后 getImage 同引用、tooltip 往返、isImageAutoSize 默认 false 与 set 往返、actionCommand 往返。
- 不支持：`new TrayIcon(Image)` 与 `new TrayIcon(Image,String)` 抛 `UnsupportedOperationException`。
headful。
