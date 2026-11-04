# java.awt.Composite 测试设计

## 1. API 功能
图像合成接口，定义 createContext。

## 2. Java 用户如何使用
匿名实现 createContext 返回 CompositeContext。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Composite.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口；黑盒: 匿名实现 createContext 返回 null。

自动化实现位于同目录 `CompositeTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：接口声明；createContext 非空；compose 透明底精确像素；dispose 安全；实现关系。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
