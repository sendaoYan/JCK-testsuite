# InternalFrameEvent 行为测试设计

## 1. API 功能
`javax.swing.event.InternalFrameEvent` 是 JInternalFrame 窗口事件，继承 `java.awt.AWTEvent`。

- **常量**：`INTERNAL_FRAME_FIRST = 25549`、`INTERNAL_FRAME_LAST = 25555`，以及 OPENED/CLOSING/CLOSED/ICONIFIED/DEICONIFIED/ACTIVATED/DEACTIVATED（依次为 FIRST + 0..6）。
- **构造器**：`InternalFrameEvent(JInternalFrame source, int id)`。
- **查询**：`getInternalFrame()`（等价于 `(JInternalFrame)getSource()`）、继承的 `getSource()`/`getID()`、`paramString()`（返回事件类型的可读字符串，未知类型返回 `"unknown type"`）。

## 2. Java 用户如何使用
用户注册 `InternalFrameListener` 读取：

```java
frame.addInternalFrameListener(new InternalFrameAdapter() {
    public void internalFrameClosing(InternalFrameEvent e) {
        JInternalFrame f = e.getInternalFrame();
        if (e.getID() == InternalFrameEvent.INTERNAL_FRAME_CLOSING) { ... }
    }
});
```

每个方法典型调用：`getSource()`/`getInternalFrame()` 取得帧 → `getID()` 判断事件类型 → `paramString()` 得到可读名称用于日志。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JInternalFrame.java:1546`：
   ```java
   e = new InternalFrameEvent(this, id);
   ```
   内部 `fireInternalFrameEvent` 依据 id 构造事件并派发。
2. `test/jdk/javax/swing/event/bug4160240.java` 直接验证 `InternalFrameEvent` 行为，是 JDK 回归测试对事件类的直接消费。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 AWTEvent、非接口/注解/枚举、public、非 abstract、无接口）。
- 黑盒：断言 9 个常量精确值；构造 OPENED/ACTIVATED 事件验证 `getSource/getInternalFrame/getID/paramString` round-trip；构造 id=42 验证 `paramString()` 返回 `"unknown type"` 的边界分支。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe（JInternalFrame 非 Window，无需显示器）。
