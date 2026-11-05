# javax.accessibility.AccessibleStateSet 测试设计

## 1. API 功能
组件的状态集合，支持增删查、去重、转数组与字符串。

按方法分组：
- 构造: (), (AccessibleState[])
- 操作: add, addAll, remove, clear
- 查询: contains, toArray, toString

## 2. Java 用户如何使用
AccessibleContext.getAccessibleStateSet 返回它。测试覆盖去重、添加/删除返回值、toString 逗号分隔与空集 null。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleStateSet.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、add/addAll/remove/contains/toArray/clear
- 黑盒: 空集 null/去重/添加删除返回值/toString 逗号/clear

自动化实现位于同目录 `AccessibleStateSetTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 空集特例：`toString()` 返回 null（锁定 quirk，与 RelationSet 的 "" 不同）、toArray 长 0、contains false
- 全操作矩阵：add×2 → "enabled,focusable"；重复 add false 且不变；remove true/再 false；addAll 到 3；toArray 防御性拷贝（改视图不影响内部）；clear 归零
- 真组件对照：JButton 状态集含 ENABLED+FOCUSABLE、不含 SELECTED
