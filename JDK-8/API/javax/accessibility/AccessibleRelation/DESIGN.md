# javax.accessibility.AccessibleRelation 测试设计

## 1. API 功能
描述对象与其他对象间的关系（labelFor/memberOf/controllerFor 等），含 key 与目标对象数组。

按方法分组：
- 常量: LABEL_FOR/LABELED_BY/MEMBER_OF/CONTROLLER_FOR/CONTROLLED_BY/FLOWS_TO 等及 *_PROPERTY
- 构造: (String key), (String key, Object target), (String key, Object[] target)
- 查询: getKey(), getTarget()
- 设置: setTarget(Object), setTarget(Object[])

## 2. Java 用户如何使用
组件用它声明与其它组件的语义关系。测试覆盖三种构造、set/get target、getTarget 防御性拷贝、常量与本地化。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleRelation.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、继承 AccessibleBundle、getKey/getTarget/setTarget
- 黑盒: 三种构造；set/get target；防御性拷贝；常量值；LABEL_FOR 本地化

自动化实现位于同目录 `AccessibleRelationTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 常量为 String 类型（非 AccessibleRelation 实例）：12 键全表 childNodeOf/controlledBy/controllerFor/embeddedBy/embeds/flowsFrom/flowsTo/labeledBy/labelFor/memberOf/parentWindowOf/subwindowOf（反射逐字段验证）
- 三构造形态：varargs Object[]、单对象、无目标（空数组）；setTarget(Object) 整体替换为单元素
- `toString()` 返回本地化显示串（"child node of"），非 key
