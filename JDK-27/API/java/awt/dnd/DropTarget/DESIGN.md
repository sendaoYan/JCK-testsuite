# java.awt.dnd.DropTarget 测试设计

## 1. API 功能
拖拽目标，管理可放置组件与监听器。

## 2. Java 用户如何使用
new DropTarget(); get/setComponent、get/setDefaultActions、setActive/isActive、add/remove DropTargetListener、get/setFlavorMap、getDropTargetContext。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DropTarget.java

## 4. 测试用例设计 + 自动化实现
白盒: 实现 DropTargetListener+Serializable；黑盒: 构造、active/actions/listener/flavorMap/context。headful（构造抛 HeadlessException）。

自动化实现位于同目录 `DropTargetTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：headless 构造器异常；headful 契约全套；DropTargetAdapter 需实现 drop。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
