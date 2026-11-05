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

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 常量 CHARACTER=1/WORD=2/SENTENCE=3
- 真文本切分矩阵（JTextArea "Hello world. Second sentence here."，34 字符，headless）：
  - CHARACTER：at 5=" "、after 5="w"、before 5="o"
  - WORD：at 0="Hello"、at 6="world"、after 0=" "（词后分隔符）、before 7=" "、before 33="here"
  - SENTENCE：at 0="Hello world. "（保留尾空格）、at 13="Second sentence here.\n"（保留尾换行）、after 0 同 13、before 13 同 0
- 选择默认：getSelectionStart/End 均 0、getSelectedText() null
- JTextField 单行变体：WORD at 4="def"、SENTENCE at 0="abc def\n"（合成尾换行）、charCount=7（不含换行）
