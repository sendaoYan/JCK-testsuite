# java.awt.TrayIcon 行为测试设计

## 1. API 功能
构造：`TrayIcon(Image)`/`(Image,String tooltip)`/`(Image,String,PopupMenu)`；属性：`getImage/setImage`、`setToolTip/getToolTip`、`isImageAutoSize/setImageAutoSize`、actionCommand；消息：`displayMessage(...)` 与 MessageType（ERROR/WARNING/INFO/NONE）。

## 2. Java 用户如何使用
```java
TrayIcon ti = new TrayIcon(img, "tip");
if (SystemTray.isSupported()) SystemTray.getSystemTray().add(ti);
```
先查 SystemTray.isSupported，再走构造与属性；不支持时走精确异常。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/TrayIcon.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractTrayIcon()`）。
对应 `TrayIconTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `TrayIcon(0x0 image)` 无条件抛 IAE（宽高 <=0），优先于 tray 支持检查（即使无 tray 也先抛 IAE）——参数校验先行，实测锁定。
- 无 tray 支持（本环境）时：`getSystemTray()` 与 `TrayIcon(有效图)`、`TrayIcon(有效图,tip)`、`TrayIcon(null)` 均抛精确 UnsupportedOperationException。
- MessageType 恰 4 常量 {ERROR,WARNING,INFO,NONE}。
- tray 可用时属性往返（image 同一引用、tooltip、autosize 默认 false、actionCommand）——环境自适应分支。
