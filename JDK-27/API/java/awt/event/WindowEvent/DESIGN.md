# java.awt.event.WindowEvent 行为测试设计

## 1. API 功能
窗口事件：WINDOW_OPENED=200..WINDOW_STATE_CHANGED=209 等 11 个 id，带 opposite 窗口、oldState/newState；getWindow/getOppositeWindow/getOldState/getNewState。

## 2. Java 用户如何使用
`new WindowEvent(window, WINDOW_STATE_CHANGED, oldState, newState)`；WindowListener/WindowStateListener/WindowFocusListener 读 getWindow/getOldState/getNewState。headless 下无法构造 Window，仅测常量与 null 源校验。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/WindowEvent.java`；`test/jdk/java/awt/event/` 窗口相关用例（headful）。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 ComponentEvent、常量/构造器/getWindow 返回 Window。黑盒：11 个 id 常量精确值；4 种构造器对 null 源抛 IllegalArgumentException；headful 分支构造 Window 验证 getWindow/getOppositeWindow/getOldState/getNewState/paramString。
对应 `WindowEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- 200..209 全部 id 精确值及相邻配对关系（closing/closed、iconified/deiconified、activated/deactivated、gained/lost focus）。
- 全部构造器 null source → IAE（EventObject 契约）。
- 序列化：类型可序列化；实例断言在 headful 分支内（headless 下跳过，不影响确定性）。
