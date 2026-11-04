# java.awt.desktop.ScreenSleepEvent 测试设计

## 1. API 功能
屏幕休眠事件。

## 2. Java 用户如何使用
new ScreenSleepEvent()。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/desktop/ScreenSleepEvent.java

## 4. 测试用例设计 + 自动化实现
白盒: final、继承关系；黑盒: 构造实例 + getter 断言。headful（无头构造抛 HeadlessException）。

自动化实现位于同目录 `ScreenSleepEventTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
