# java.awt.event.HierarchyEvent 行为测试设计

## 1. API 功能
组件层级变化事件：HIERARCHY_CHANGED=1400/ANCESTOR_MOVED/ANCESTOR_RESIZED，带 changed 组件、changedParent 容器、changeFlags（PARENT_CHANGED/DISPLAYABILITY_CHANGED/SHOWING_CHANGED）。

## 2. Java 用户如何使用
`new HierarchyEvent(source, HIERARCHY_CHANGED, changed, parent, PARENT_CHANGED|SHOWING_CHANGED)`；HierarchyListener/HierarchyBoundsListener 读 getChanged/getChangedParent/getChangeFlags。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/HierarchyEvent.java`；`test/jdk/java/awt/event/HierarchyEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、常量/构造器。黑盒：构造→回读 id/source/getComponent/getChanged/getChangedParent/getChangeFlags；组合 flags；8 个常量精确值；paramString 含 HIERARCHY_CHANGED/PARENT_CHANGED 等。
对应 `HierarchyEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
