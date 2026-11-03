# javax.accessibility.AccessibleText 测试设计

## 1. API 功能
只读文本访问：字符计数/光标/选区/按字符词句取值/属性/边界。

按方法分组：
- 常量: CHARACTER=1, WORD=2, SENTENCE=3
- 查询: getIndexAtPoint, getCharacterBounds, getCharCount, getCaretPosition, getAtIndex/AfterIndex/BeforeIndex, getCharacterAttribute, getSelectionStart/End, getSelectedText

## 2. Java 用户如何使用
文本组件通过它暴露文本内容给辅助技术。测试用文本模型断言常量、字符取值、选区文本、属性与边界。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleText.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/AccessibleTextTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、关键方法
- 黑盒: CHARACTER/WORD/SENTENCE 常量；charCount/caret/取值/选区/属性/边界

自动化实现位于同目录 `AccessibleTextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
