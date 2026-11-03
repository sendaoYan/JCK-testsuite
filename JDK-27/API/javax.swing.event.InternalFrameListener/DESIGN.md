# javax.swing.event.InternalFrameListener 测试设计

## 1. API 功能

`InternalFrameListener` 监听 `JInternalFrame`（桌面内部窗口）生命周期与状态变化，继承 `java.util.EventListener`，共 7 个回调：

- `internalFrameOpened(InternalFrameEvent e)`：打开。
- `internalFrameClosing(InternalFrameEvent e)`：正在关闭（可拦截）。
- `internalFrameClosed(InternalFrameEvent e)`：已关闭。
- `internalFrameIconified(InternalFrameEvent e)`：图标化（最小化）。
- `internalFrameDeiconified(InternalFrameEvent e)`：取消图标化。
- `internalFrameActivated(InternalFrameEvent e)`：激活。
- `internalFrameDeactivated(InternalFrameEvent e)`：失活。

事件 `InternalFrameEvent` 携带 `getInternalFrame()` 与 `getID()`（`INTERNAL_FRAME_OPENED/CLOSING/CLOSED/ICONIFIED/DEICONIFIED/ACTIVATED/DEACTIVATED`）。

## 2. Java 用户如何使用

构造：实现接口（或继承 `InternalFrameAdapter`）并注册：

```java
JInternalFrame f = new JInternalFrame("t");
InternalFrameListener l = new InternalFrameListener() {
    public void internalFrameOpened(InternalFrameEvent e) { /* 打开 */ }
    public void internalFrameClosing(InternalFrameEvent e) { /* 关闭前 */ }
    public void internalFrameClosed(InternalFrameEvent e) { /* 已关闭 */ }
    public void internalFrameIconified(InternalFrameEvent e) { /* 图标化 */ }
    public void internalFrameDeiconified(InternalFrameEvent e) { /* 还原 */ }
    public void internalFrameActivated(InternalFrameEvent e) { /* 激活 */ }
    public void internalFrameDeactivated(InternalFrameEvent e) { /* 失活 */ }
};
f.addInternalFrameListener(l);
```

典型组合链路：`addInternalFrameListener → 显示 f → internalFrameOpened → 点标题栏激活 → internalFrameActivated → 图标化 → internalFrameIconified → 还原 → internalFrameDeiconified → 关闭 → internalFrameClosing/internalFrameClosed`。7 个方法覆盖窗口完整生命周期。

## 3. 开源代码实际用法调研

1. JDK `JInternalFrame` 提供 `addInternalFrameListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JInternalFrame.java:1492`），并在打开/关闭/图标化/激活路径触发 `InternalFrameEvent`。
2. `BasicInternalFrameUI.BasicInternalFrameListener` 直接实现 `InternalFrameListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicInternalFrameUI.java:1454`），负责把内部窗口生命周期联动到 LAF。
3. `test/jdk/javax/swing/JInternalFrame/` 多个用例用 `addInternalFrameListener` 验证打开/关闭时序。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`InternalFrameListenerTest.testBlackBox()`）：
- 匿名实现 7 个回调，记录最近回调名与事件。
- 构造 7 个 `InternalFrameEvent`（对应 7 个 id），逐一调用 7 个方法。
- 断言每次回调名正确、事件实例一致、`getInternalFrame()` 与 `getID()` round-trip；7 个 id 常量两两互异。

白盒保留原有结构断言，新增 7 方法签名/抽象性与 `declared method count == 7`。
