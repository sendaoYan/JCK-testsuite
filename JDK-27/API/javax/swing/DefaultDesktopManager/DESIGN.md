# javax.swing.DefaultDesktopManager 设计案

## 1. API 功能
`DefaultDesktopManager` 是 `DesktopManager` 的默认实现，管理 JInternalFrame 的打开/关闭/最大化/最小化/图标化/拖动/缩放。方法分组：
- 生命周期：`openFrame/closeFrame/maximizeFrame/minimizeFrame/iconifyFrame/deiconifyFrame/activateFrame/deactivateFrame`。
- 拖动/缩放：`beginDraggingFrame/dragFrame/endDraggingFrame/beginResizingFrame/resizeFrame/endResizingFrame`。
- 位置：`setBoundsForFrame(JComponent, x, y, w, h)`。

## 2. Java 用户如何使用
用户通常由 `JDesktopPane` 自动使用它管理内部窗口；也可自定义子类覆盖拖动/缩放行为：
```java
DesktopManager dm = new DefaultDesktopManager();
dm.setBoundsForFrame(internalFrame, 10, 20, 400, 300);
```
典型链路：`new DefaultDesktopManager` → `setBoundsForFrame` → `dragFrame` → `resizeFrame` → `activateFrame/deactivateFrame`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JDesktopPane.java` 默认持有 `DefaultDesktopManager` 管理内部窗口（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JDesktopPane.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicDesktopPaneUI.java` 通过 `DesktopManager` 处理拖动/缩放。
- JDK 测试 `test/jdk/javax/swing/JInternalFrame/` 覆盖内部窗口管理器行为。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `DesktopManager`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `setBoundsForFrame/dragFrame/resizeFrame` 的可观察边界变化，以及无 desktop 时各生命周期方法的 no-op smoke。
