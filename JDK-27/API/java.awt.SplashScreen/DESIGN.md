# java.awt.SplashScreen 测试设计

## 1. API 功能
启动画面，由 -splash 参数创建。

## 2. Java 用户如何使用
getSplashScreen()（无 -splash 返回 null）；isVisible/getBounds/createGraphics 需有效实例。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/SplashScreen.java

## 4. 测试用例设计 + 自动化实现
白盒: final、声明 getSplashScreen/isVisible；黑盒: 无 -splash 时 getSplashScreen()==null。headful（无头抛 HeadlessException）。

自动化实现位于同目录 `SplashScreenTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
