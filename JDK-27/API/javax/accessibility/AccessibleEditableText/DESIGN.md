# javax.accessibility.AccessibleEditableText 测试设计

## 1. API 功能
扩展 AccessibleText 提供可编辑文本操作：设置/插入/删除/剪切/粘贴/替换/选择/设置属性。

按方法分组：
- 编辑: setTextContents, insertTextAtIndex, delete, cut, paste, replaceText
- 查询: getTextRange
- 选择/属性: selectText, setAttributes
- 继承: AccessibleText 全部方法

## 2. Java 用户如何使用
文本框实现该接口，辅助技术可改写文本内容。测试用一个 StringBuilder 模型，串联 setTextContents→getTextRange→insert→replace→delete→cut→paste→selectText 组合断言。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTextComponent.java (AccessibleJTextComponent)
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/AccessibleTextTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、继承 AccessibleText、声明编辑方法
- 黑盒: 全流程文本编辑组合断言

自动化实现位于同目录 `AccessibleEditableTextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
