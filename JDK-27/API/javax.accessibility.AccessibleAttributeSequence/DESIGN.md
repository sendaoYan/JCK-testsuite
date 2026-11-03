# javax.accessibility.AccessibleAttributeSequence 测试设计

## 1. API 功能
收集共享同一组文本属性的文本区间（startIndex/endIndex/attributes），供 AccessibleContext 生成 ACCESSIBLE_TEXT_ATTRIBUTES_CHANGED 事件。

按方法分组：
- 字段: public int startIndex, endIndex; public AttributeSet attributes
- 构造: AccessibleAttributeSequence(int, int, AttributeSet)

## 2. Java 用户如何使用
实现者构造序列对象填充区间与属性后放入属性变更事件。测试构造带值/空值/负下标实例并读写公开字段。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleAttributeSequence.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、三个公开字段
- 黑盒: 构造读回、字段可变、空/负值保留

自动化实现位于同目录 `AccessibleAttributeSequenceTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
