# javax.swing.text.DefaultStyledDocument.SectionElement — DESIGN

## 1. API 功能
`AbstractDocument.BranchElement` 的 protected 子类，作为 `DefaultStyledDocument` 的文档根，代表由段落组成的 section。方法分组：
- 构造：`SectionElement()`。
- 覆盖：`getName()` 返回 `AbstractDocument.SectionElementName`（"section"）。
- 继承自 `BranchElement`：`getStartOffset/getEndOffset/getElement/getElementCount/getElementIndex/isLeaf/getAllowsChildren/children`。

## 2. Java 用户如何使用
不直接实例化（protected），而是经 `DefaultStyledDocument.getDefaultRootElement()` 取得：`Element root = new DefaultStyledDocument().getDefaultRootElement();` 其 `getName()` 为 "section"，子元素是段落（`paragraph`）。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:915`：`BranchElement section = new SectionElement();`（在 `createDefaultRoot` 中）。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:1154`：`protected class SectionElement extends BranchElement`，`getName()` 返回 `SectionElementName`。
- `src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java`：`SectionElementName` 常量定义为 `"section"`。

## 4. 测试用例设计 + 自动化实现（`SectionElementTest.testBlackBox`）
- 验证 `getDefaultRootElement()` 是 `SectionElement` 实例、`getName()=="section"`、非叶子、允许子元素、初始 1 个段落。
- 区间：`getStartOffset()==0`、`getEndOffset()==getLength()+1`（含尾随换行）。
- 插入多行后段落数更新、`getParagraphElement` 映射到 section 子元素、父元素为 null、所属文档正确。

## 5. 契约深化（本轮追加）
（17 断言）：DefaultStyledDocument 默认根就是 SectionElement（protected 类，经 getClass().getName() 判定）；名字 "section"；为 branch；空文档恰一段；插入 "ab\ncd" 后区间 [0,6) 且子元素是名为 "paragraph" 的段；getDocument 回链、无父元素。
