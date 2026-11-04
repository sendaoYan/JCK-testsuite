# java.awt.Stroke 测试设计

## 1. API 功能
描边接口。

## 2. Java 用户如何使用
匿名实现 createStrokedShape 返回 null。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Stroke.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口；黑盒: createStrokedShape null。

自动化实现位于同目录 `StrokeTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：接口单方法；两 stroke 输出非空；包围盒高度 1→5 单调增大。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
