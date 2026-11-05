# javax.accessibility.AccessibleRole 测试设计

## 1. API 功能
对象的角色枚举（push button/table/list 等），继承 AccessibleBundle。

按方法分组：
- 常量: ALERT/PUSH_BUTTON/TABLE/UNKNOWN 等大量角色
- 继承: toDisplayString(), toString()
- 构造: protected AccessibleRole(String key)

## 2. Java 用户如何使用
组件用预定义角色或自定义子类描述用途。测试断言常量互异、已知角色本地化、未知 key 回退、toString 等价。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleRole.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、继承 AccessibleBundle、toDisplayString
- 黑盒: 常量非空互异；PUSH_BUTTON/ALERT 本地化；自定义 key 回退；toString==toDisplayString

自动化实现位于同目录 `AccessibleRoleTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 63 常量全表（ALERT..PROGRESS_MONITOR）逐一非空且 ENGLISH 显示串非空
- 本地化精确串：alert/push button/label（EN）；bouton（FR）；`toString() == toDisplayString(ENGLISH)`
- equals/hashCode 自反稳定、异角色不等、equals(null)/equals(foreign) false；跨 Bundle（vs State）equals false
- 构造器 protected（自定义角色须经子类）；匿名子类 key "deep-role" 原样回退
- 不.Serializable（serRoundTrip 抛 NotSerializableException）
- 版本差异：JDK-8 可开放反射读取 protected key 字段；JDK-9+ 受模块封装限制
