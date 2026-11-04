# javax.accessibility.AccessibleState 测试设计

## 1. API 功能
组件的状态枚举（active/enabled/visible/showing 等），继承 AccessibleBundle。

按方法分组：
- 常量: ACTIVE/ENABLED/VISIBLE/SHOWING 等大量状态
- 继承: toDisplayString(), toString()
- 构造: protected AccessibleState(String key)

## 2. Java 用户如何使用
组件用状态集描述当前状态。测试断言常量互异、已知状态本地化、未知 key 回退。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleState.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、继承 AccessibleBundle、toDisplayString
- 黑盒: 常量非空互异；ACTIVE/ENABLED 本地化；自定义 key 回退；toString==toDisplayString

自动化实现位于同目录 `AccessibleStateTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
