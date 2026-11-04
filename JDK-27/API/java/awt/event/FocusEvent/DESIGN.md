# java.awt.event.FocusEvent 行为测试设计

## 1. API 功能
焦点变化事件，带 opposite 相对组件、temporary 临时标志、Cause 枚举（11 个值），常量 FOCUS_GAINED=1004/FOCUS_LOST=1005。

## 2. Java 用户如何使用
`new FocusEvent(component, FOCUS_GAINED, true, opposite, Cause.TRAVERSAL_FORWARD)`；FocusListener 读 isTemporary/getOppositeComponent/getCause。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/FocusEvent.java`（Cause 枚举、paramString）；`test/jdk/java/awt/event/FocusEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 ComponentEvent、常量/构造器、Cause 为 11 常量枚举。黑盒：构造→回读 id/isTemporary/opposite/cause；默认 cause UNKNOWN；4 参显式 cause；3 参 opposite null；Cause.values/valueOf；paramString 含 temporary/permanent 与 cause。
对应 `FocusEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- FOCUS_GAINED/LOST=1004/1005、2/3/4 参构造器、null opposite 合法。
- 消费/toString 略（继承 ComponentEvent 语义）。
- JDK-27 特有：Cause 枚举（11 常量、values/valueOf、默认 UNKNOWN、显式 Cause 回读、paramString cause=）、5 参构造器、序列化 readResolve 使老流 cause 默认 UNKNOWN。
- JDK-8 特有：无 Cause 嵌套类型（白盒断言其不存在），无 5 参构造器。
- 序列化：id/temporary(+cause, JDK-27) 保留；opposite transient。
