# java.awt.DefaultFocusTraversalPolicy 测试设计

## 1. API 功能
默认焦点遍历策略，跳过不可聚焦组件，按容器子组件顺序遍历。

## 2. Java 用户如何使用
new DefaultFocusTraversalPolicy(); getFirstComponent/getComponentAfter/getComponentBefore/getLastComponent/getDefaultComponent。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/DefaultFocusTraversalPolicy.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 ContainerOrderFocusTraversalPolicy、声明 accept；黑盒: 可见 Frame 两按钮的遍历顺序。headful。

自动化实现位于同目录 `DefaultFocusTraversalPolicyTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
