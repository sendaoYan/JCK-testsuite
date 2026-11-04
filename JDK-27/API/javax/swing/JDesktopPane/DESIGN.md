# javax.swing.JDesktopPane 测试设计

## 1. API 功能
- 常量：`LIVE_DRAG_MODE(0)`、`OUTLINE_DRAG_MODE(1)`。
- 拖拽/管理器：`setDragMode/getDragMode`、`getDesktopManager/setDesktopManager`。
- 内部框架管理：`add(JInternalFrame)`、`getAllFrames()`、`getAllFramesInLayer(int)`、`getSelectedFrame/setSelectedFrame`、`remove(Component)`。
- UI：`getUIClassID()` 返回 "DesktopPaneUI"。

## 2. Java 用户如何使用
典型组合：`new JDesktopPane()` → `add(new JInternalFrame(...))` → `getAllFrames()` 遍历、`setSelectedFrame(f)` 标记活动框架；`remove(f)` 关闭后同步移除；用 `setDragMode(OUTLINE_DRAG_MODE)` 改变拖动表现。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JInternalFrame/bug4305284.java`、`bug4320889.java`、`Test6505027.java`：在 JDesktopPane 中添加 JInternalFrame 并验证选择/层行为。
- JDK 源码 `javax.swing.JInternalFrame.getDesktopPane`：向上查找 JDesktopPane 祖先，是 MDI 应用的常用链路。
- 典型 MDI 桌面程序用 JDesktopPane + JInternalFrame 实现多文档界面。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：拖拽模式常量与往返、默认桌面管理器非 null、自定义管理器往返；初始无框架、无选中框架；`add` 两个 JInternalFrame 后 `getAllFrames` 顺序与 `getAllFramesInLayer(DEFAULT_LAYER)`；`setSelectedFrame/getSelectedFrame` 往返；`remove` 后 `getAllFrames` 更新。见 `JDesktopPaneTest.java` 的 `testBlackBox()`。
