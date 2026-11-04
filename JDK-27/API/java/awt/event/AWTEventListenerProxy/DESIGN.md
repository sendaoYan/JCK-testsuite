# java.awt.event.AWTEventListenerProxy 行为测试设计

## 1. API 功能
AWTEventListener 的代理包装（继承 EventListenerProxy），附加 eventMask，eventDispatched 转发给被包装监听器；getEventMask 返回掩码。

## 2. Java 用户如何使用
`new AWTEventListenerProxy(AWTEvent.KEY_EVENT_MASK, listener)` 后注册到 Toolkit.addAWTEventListener，按掩码过滤事件。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/AWTEventListenerProxy.java`；`Toolkit.addAWTEventListener` 用其存掩码。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 EventListenerProxy、实现 AWTEventListener、构造器/getEventMask。黑盒：getEventMask 回读（含位或）；getListener 返回被包装者；eventDispatched 转发计数；instanceof 关系；独立掩码与零掩码。
对应 `AWTEventListenerProxyTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- 代理委托：eventDispatched 转发到被包装 listener（副作用计数）；getListener() 返回非 null。
- 结构：EventListener/EventListenerProxy/AWTEventListener 三重类型关系（AWTEventListener 位于 java.awt.event 包）。
