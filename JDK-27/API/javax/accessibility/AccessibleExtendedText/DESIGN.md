# javax.accessibility.AccessibleExtendedText 测试设计

## 1. API 功能
扩展文本访问，返回文本区间/文本序列/文本边界。

按方法分组：
- 常量: LINE=4, ATTRIBUTE_RUN=5
- 查询: getTextRange, getTextSequenceAt/After/Before, getTextBounds

## 2. Java 用户如何使用
富文本组件按行/属性区间返回文本序列。测试用匿名实现返回固定序列与边界矩形，断言常量精确值。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleExtendedText.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、无父接口、五个方法
- 黑盒: LINE/ATTRIBUTE_RUN 常量；序列与边界返回

自动化实现位于同目录 `AccessibleExtendedTextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 契约深化（黑盒行为）
`testContractDeep()`（2026-11 契约深化）追加断言：
- 关键结构事实：AccessibleExtendedText 是独立接口，不是 AccessibleText 的子接口（isAssignableFrom 为 false）
- 自有常量恰两个：LINE=4、ATTRIBUTE_RUN=5（扩展型分段种类，AccessibleText 的 1/2/3 之外）
- JTextArea（headless）文本实现本接口：getTextSequenceAt(WORD,6) → 6-11 "world"；getTextSequenceAt(CHARACTER,0) → 0-1 "H"；getTextSequenceAfter(WORD,0) → 5-6 " "（词后分隔符）
- 越界 getTextSequenceAt(WORD,100) → null；getTextBounds(0,5) → null（无布局的无头契约）
