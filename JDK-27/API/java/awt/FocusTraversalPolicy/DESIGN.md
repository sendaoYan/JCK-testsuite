# java.awt.FocusTraversalPolicy 测试设计

## 1. API 功能
焦点遍历策略抽象基类，定义焦点在组件间移动的顺序。

## 2. Java 用户如何使用
匿名实现 5 个抽象方法；getInitialComponent(Window) 委托 getDefaultComponent。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/FocusTraversalPolicy.java

## 4. 测试用例设计 + 自动化实现
白盒: 抽象、继承 Object、声明 getInitialComponent；黑盒: 匿名实现、getInitialComponent(null) 委托。

自动化实现位于同目录 `FocusTraversalPolicyTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
