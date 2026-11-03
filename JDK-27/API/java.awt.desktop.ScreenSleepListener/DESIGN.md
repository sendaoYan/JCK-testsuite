# java.awt.desktop.ScreenSleepListener 测试设计

## 1. API 功能
屏幕休眠监听接口。

## 2. Java 用户如何使用
匿名实现各方法。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/desktop/ScreenSleepListener.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口；黑盒: 匿名实现各方法可调用。

自动化实现位于同目录 `ScreenSleepListenerTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
