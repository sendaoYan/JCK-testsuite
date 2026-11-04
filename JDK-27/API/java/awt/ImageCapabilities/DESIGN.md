# java.awt.ImageCapabilities 测试设计

## 1. API 功能
描述图像加速能力（是否加速、是否真易变），可 clone。

## 2. Java 用户如何使用
new ImageCapabilities(accelerated)，isAccelerated/isTrueVolatile/clone。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/ImageCapabilities.java

## 4. 测试用例设计 + 自动化实现
白盒: implements Cloneable；黑盒: 两种构造、isAccelerated/isTrueVolatile、clone 独立性。

自动化实现位于同目录 `ImageCapabilitiesTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：accelerated 往返；isTrueVolatile false；无 equals（恒等）；clone 可用。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
