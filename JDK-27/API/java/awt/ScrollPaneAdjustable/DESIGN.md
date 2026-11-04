# java.awt.ScrollPaneAdjustable 测试设计

## 1. API 功能
ScrollPane 的滚动条可调项，实现 Adjustable。

## 2. Java 用户如何使用
只能从 ScrollPane 获取（构造器包私有）；set/get value/minimum/maximum/unit/block/visibleAmount。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/ScrollPaneAdjustable.java

## 4. 测试用例设计 + 自动化实现
白盒: final、implements Adjustable+Serializable、声明关键方法；黑盒降级: 包私有构造无法直接实例化。

自动化实现位于同目录 `ScrollPaneAdjustableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
