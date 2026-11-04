# java.awt.event.MouseEvent 行为测试设计

## 1. API 功能
鼠标事件：x/y 相对坐标、xAbs/yAbs 绝对坐标、clickCount、button、popupTrigger；getPoint/getLocationOnScreen/translatePoint/getButton/getModifiersEx。

## 2. Java 用户如何使用
`new MouseEvent(component, MOUSE_PRESSED, when, mods, x, y, xAbs, yAbs, clickCount, popupTrigger, button)`；MouseListener/MouseMotionListener 读 getX/getButton/getClickCount。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/MouseEvent.java`；`test/jdk/java/awt/event/MouseEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 InputEvent、常量/构造器。黑盒：构造→回读 x/y/point/xAbs/yAbs/locationOnScreen/clickCount/button/popupTrigger；translatePoint 只移相对坐标；8 参默认 NOBUTTON；11 个常量精确值；getModifiersEx；getMouseModifiersText；paramString 含坐标/button/clickCount。
对应 `MouseEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- 全部 id 精确值（500..507 含 MOUSE_ENTERED=504/MOUSE_EXITED=505）、NOBUTTON/BUTTON1/2/3=0/1/2/3。
- 负坐标/负绝对坐标/0 clickCount 无校验回读；translatePoint 双向与 (0,0) no-op；getPoint 防御性拷贝。
- getMouseModifiersText：Button1/2/3（DOWN_MASK 位）；旧 BUTTON2_MASK(==ALT_MASK) 渲染 "Alt+Button2"。
- 消费语义（InputEvent 公开 consume()/isConsumed()）。
- 序列化：坐标/绝对坐标/clickCount/button/popupTrigger 保留。
