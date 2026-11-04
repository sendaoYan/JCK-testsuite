# java.awt.event.ContainerEvent 行为测试设计

## 1. API 功能
容器子组件增删事件，带 child 子组件，继承 ComponentEvent，常量 COMPONENT_ADDED=300/COMPONENT_REMOVED=301。

## 2. Java 用户如何使用
`new ContainerEvent(container, COMPONENT_ADDED, child)`；ContainerListener 读 getContainer/getChild。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/ContainerEvent.java`；`Container.java` add/remove 触发。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 ComponentEvent、常量与构造器。黑盒：构造→getID/getContainer/getChild/getComponent 回读；4 个常量精确值；paramString 含 child 名字。
对应 `ContainerEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
