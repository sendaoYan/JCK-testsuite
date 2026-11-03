# javax.accessibility.AccessibleTextSequence 测试设计

## 1. API 功能
收集一段文本（startIndex/endIndex/text），供 AccessibleExtendedText 返回。

按方法分组：
- 字段: public int startIndex, endIndex; public String text
- 构造: AccessibleTextSequence(int, int, String)

## 2. Java 用户如何使用
实现者构造序列返回文本区间。测试构造读回与字段可变。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleTextSequence.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、三个公开字段
- 黑盒: 构造读回、字段可变、空/负值保留

自动化实现位于同目录 `AccessibleTextSequenceTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
