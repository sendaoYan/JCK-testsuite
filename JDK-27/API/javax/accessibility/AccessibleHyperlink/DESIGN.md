# javax.accessibility.AccessibleHyperlink 测试设计

## 1. API 功能
抽象类，表示超文本中的一个链接，实现 AccessibleAction 并提供有效性/起止下标/动作对象与锚点。

按方法分组：
- 状态: isValid()
- 动作: getAccessibleActionCount/Description, doAccessibleAction
- 对象/锚点: getAccessibleActionObject(int), getAccessibleActionAnchor(int)
- 位置: getStartIndex(), getEndIndex()

## 2. Java 用户如何使用
辅助技术遍历超文本链接，读取起止下标并执行动作。测试用匿名子类返回固定值，断言动作成功/失败与起止下标。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleHyperlink.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象类、implements AccessibleAction、声明 isValid 等
- 黑盒: isValid/动作/描述/对象/锚点/起止下标

自动化实现位于同目录 `AccessibleHyperlinkTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
