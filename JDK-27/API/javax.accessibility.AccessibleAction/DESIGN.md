# javax.accessibility.AccessibleAction 测试设计

## 1. API 功能
定义可执行动作：动作个数、动作描述、执行动作，并定义 CLICK/INCREMENT/DECREMENT/TOGGLE_EXPAND/TOGGLE_POPUP 常量。

按方法分组：
- 常量: CLICK, INCREMENT, DECREMENT, TOGGLE_EXPAND, TOGGLE_POPUP
- 查询: getAccessibleActionCount(), getAccessibleActionDescription(int)
- 操作: doAccessibleAction(int) -> boolean

## 2. Java 用户如何使用
Swing 按钮通过 doAccessibleAction(0) 触发点击；辅助技术先 getAccessibleActionCount 再逐个取描述、执行。测试用匿名实现返回固定动作集，验证常量值与执行结果。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JButton.java (AccessibleJButton 实现 AccessibleAction)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/AccessibleActionsTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、无父接口、三个方法
- 黑盒: 五个常量精确值；匿名实现 count/description/doAccessibleAction 组合断言

自动化实现位于同目录 `AccessibleActionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
