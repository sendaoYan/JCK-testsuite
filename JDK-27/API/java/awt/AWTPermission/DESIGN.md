# java.awt.AWTPermission 测试设计

## 1. API 功能
AWT 权限，继承 BasicPermission，name 为权限名，可带 actions。

## 2. Java 用户如何使用
new AWTPermission(name) / new AWTPermission(name, actions)，getName/getActions/implies/equals/hashCode。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/AWTPermission.java

## 4. 测试用例设计 + 自动化实现
白盒: final、继承 BasicPermission；黑盒: 两种构造、name/actions、implies/equals/hashCode。

自动化实现位于同目录 `AWTPermissionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：getActions 空；implies 通配/前缀；null NPE；层次。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
