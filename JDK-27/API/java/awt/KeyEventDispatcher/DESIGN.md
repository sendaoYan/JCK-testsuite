# java.awt.KeyEventDispatcher 测试设计

## 1. API 功能
键盘事件分发器接口。

## 2. Java 用户如何使用
匿名实现 dispatchKeyEvent 返回 false。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/KeyEventDispatcher.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口；黑盒: dispatchKeyEvent 返回 false。

自动化实现位于同目录 `KeyEventDispatcherTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
