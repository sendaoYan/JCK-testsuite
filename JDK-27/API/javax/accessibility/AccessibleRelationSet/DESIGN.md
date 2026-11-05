# javax.accessibility.AccessibleRelationSet 测试设计

## 1. API 功能
一组 AccessibleRelation，支持增删查与同 key 目标合并。

按方法分组：
- 构造: (), (AccessibleRelation[])
- 操作: add, addAll, remove, clear
- 查询: contains(String), get(String), size, toArray, toString

## 2. Java 用户如何使用
AccessibleContext.getAccessibleRelationSet 返回它；添加同 key 关系时合并目标数组。测试覆盖 add/合并/contains/get/remove/clear/size/toArray/toString。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleRelationSet.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、add/remove/contains/get/size/clear 方法
- 黑盒: 空集行为；同 key 合并目标；构造数组/addAll/remove/clear/toArray

自动化实现位于同目录 `AccessibleRelationSetTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 空集：size 0、toString ""（注意与 StateSet 的 null 不同）、contains/get 均否定、toArray 空
- 重复 add 语义（关键发现）：同键 add 返回 true 且按目标并集合并——[x]+[z] → [x,z]；再 add 多目标 [m1,m2] → [x,z,m1,m2]（保持插入序，不替换）
- contains(String)/get(String) 按键检索；get 缺键 null；remove(AccessibleRelation) 按键移除、二次 false；addAll/clear
- toString 顺序 "label for,controlledBy"（显示串逗号连接）
