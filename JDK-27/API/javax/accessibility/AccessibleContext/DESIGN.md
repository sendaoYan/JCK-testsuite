# javax.accessibility.AccessibleContext 测试设计

## 1. API 功能
所有可访问对象的最小信息：name/description/parent/relationSet + 属性变更事件；以及获取各子接口的默认返回 null 的方法。

按方法分组：
- 名称/描述: get/setAccessibleName, get/setAccessibleDescription
- 父子: get/setAccessibleParent
- 事件: add/removePropertyChangeListener, firePropertyChange
- 子接口: getAccessibleAction/Component/Selection/Text/EditableText/Value/Icon/Table -> null 默认
- 关系: getAccessibleRelationSet()
- 抽象: getAccessibleRole/StateSet/IndexInParent/ChildrenCount/Child/Locale

## 2. Java 用户如何使用
Swing 组件通过 AccessibleContext 暴露 name/description 与角色状态；setAccessibleName 会触发 ACCESSIBLE_NAME_PROPERTY 事件。测试用最小子类验证 set/get、事件分发、默认 null 与空 relationSet。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleContext.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象类、声明 name/description/role 等方法
- 黑盒: name/description 回读+事件；parent 回读；子接口默认 null；relationSet 非空；firePropertyChange；removeListener

自动化实现位于同目录 `AccessibleContextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
