# java.awt.Paint 测试设计

## 1. API 功能
绘制接口，扩展 Transparency。

## 2. Java 用户如何使用
匿名实现 createContext/getTransparency。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Paint.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口、继承 Transparency；黑盒: 匿名实现。

自动化实现位于同目录 `PaintTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：接口层级；四实现 createContext 非空；OPAQUE 四例；半透明分支；TexturePaint 属同族。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
