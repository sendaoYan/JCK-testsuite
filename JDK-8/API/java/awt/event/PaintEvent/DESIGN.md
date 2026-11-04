# java.awt.event.PaintEvent 行为测试设计

## 1. API 功能
绘制事件：PAINT=800/UPDATE=801，带 updateRect 裁剪矩形（可空），getUpdateRect/setUpdateRect。

## 2. Java 用户如何使用
`new PaintEvent(component, PaintEvent.PAINT, new Rectangle(...))`；绘制时读 getUpdateRect 做局部重绘。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/PaintEvent.java`；`Component.repaint` 产生 UPDATE/PAINT。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 ComponentEvent、常量/构造器。黑盒：构造→回读 id/source/updateRect；setUpdateRect；null 矩形；4 个常量精确值；paramString 含 updateRect。
对应 `PaintEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- PAINT/UPDATE=800/801、FIRST/LAST；null/零尺寸 updateRect 合法；setUpdateRect(null) 合法。
- paramString 精确等值（PAINT,updateRect=...、UPDATE,updateRect=null、unknown type）。
- 序列化：id/updateRect 保留。
