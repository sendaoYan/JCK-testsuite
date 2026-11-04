# java.awt.LayoutManager2 测试设计

## 1. API 功能
布局管理器2接口，扩展 LayoutManager 加入约束与对齐。

## 2. Java 用户如何使用
匿名实现全部方法。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/LayoutManager2.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口、继承 LayoutManager；黑盒: 匿名实现各方法可调用。

自动化实现位于同目录 `LayoutManager2Test.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：接口层级；五新方法回调；约束 add 路由；容器 alignment 委托 0.25/0.75；四内建类分类。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
