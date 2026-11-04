# javax.accessibility.Accessible 测试设计

## 1. API 功能
无障碍主接口，任何支持无障碍的组件都实现它，唯一方法是 getAccessibleContext()，返回组件的 AccessibleContext。

按方法分组：
- 查询: getAccessibleContext() -> AccessibleContext

## 2. Java 用户如何使用
组件实现 Accessible 后，辅助技术通过 a.getAccessibleContext() 拿到上下文，再读 name/role/state；不可访问的子类可返回 null。测试用匿名实现返回固定/为 null 的上下文，断言身份与可达状态。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JButton.java 等所有 Swing 组件都实现 Accessible
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/AccessibleActionsTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、无父接口、声明 getAccessibleContext
- 黑盒: 返回同一上下文实例；上下文中 setName 后经 Accessible 可达；返回 null 上下文

自动化实现位于同目录 `AccessibleTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
