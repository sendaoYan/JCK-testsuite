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
