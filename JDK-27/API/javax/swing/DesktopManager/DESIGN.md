# javax.swing.DesktopManager 设计案

## 1. API 功能
`DesktopManager` 是内部窗口（JInternalFrame）管理接口。方法分组：
- 生命周期：`openFrame/closeFrame/maximizeFrame/minimizeFrame/iconifyFrame/deiconifyFrame/activateFrame/deactivateFrame`。
- 拖动/缩放：`beginDraggingFrame/dragFrame/endDraggingFrame/beginResizingFrame/resizeFrame/endResizingFrame`。
- 位置：`setBoundsForFrame(JComponent, x, y, w, h)`。

## 2. Java 用户如何使用
用户由 `JDesktopPane` 使用 `DefaultDesktopManager`，或实现自定义管理器：
```java
DesktopManager dm = new DefaultDesktopManager();
dm.setBoundsForFrame(frame, 0, 0, 400, 300);
```
典型链路：`setBoundsForFrame` → `openFrame` → `activateFrame` → `dragFrame`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JDesktopPane.java` 持有 `DesktopManager` 管理内部窗口（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JDesktopPane.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicDesktopPaneUI.java` 通过 `DesktopManager` 处理拖动/缩放。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 isInterface、无父接口。
- 行为（黑盒）：`testBlackBox()` 用 `DefaultDesktopManager` 验证 `setBoundsForFrame` 的边界变化与生命周期方法 smoke。
