# javax.accessibility.AccessibilityProvider 测试设计

## 1. API 功能
无障碍服务提供者 SPI，抽象 getName()/activate()。

按方法分组：
- 查询: getName()
- 操作: activate()
- 构造: protected AccessibilityProvider()

## 2. Java 用户如何使用
平台实现通过 ServiceLoader 加载并 activate。测试用匿名子类返回名称并置位激活标志。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibilityProvider.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象类、getName/activate
- 黑盒: 匿名子类 getName/activate 置位

自动化实现位于同目录 `AccessibilityProviderTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
