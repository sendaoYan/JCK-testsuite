# java.awt.datatransfer.FlavorTable 测试设计

## 1. API 功能
flavor 映射表接口，扩展 FlavorMap，单值查询。

## 2. Java 用户如何使用
匿名实现 getNativesForFlavor/getFlavorsForNative + 继承方法。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/FlavorTable.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口、继承 FlavorMap；黑盒: 匿名实现返回空 List。

自动化实现位于同目录 `FlavorTableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（本轮追加）
- 继承 FlavorMap 并新增 getNativesForFlavor/getFlavorsForNative List 形态方法；自定义实现 null/空语义。
