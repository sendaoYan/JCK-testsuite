# javax.swing.text.html.AccessibleHTML.TextElementInfo.TextAccessibleContext — DESIGN

## 1. API 功能
`public class TextAccessibleContext extends HTMLAccessibleContext implements AccessibleText`，表示文本元素（CONTENT/CAPTION）的可访问信息。关键方法：
- `getAccessibleRole()` 返回 `AccessibleRole.TEXT`。
- `getCharCount()`：元素跨度（`endOffset - startOffset`）。
- `getCaretPosition()`：委托给编辑器；视图/容器不存在时返回 -1。
- `getSelectionStart/End/SelectedText()`：委托给 JEditorPane。
- `getCharacterAttribute(i)`：返回字符元素的 AttributeSet。
- `getAtIndex/getAfterIndex/getBeforeIndex(part, index)`：按字符/词/句取文本。
- `getAccessibleText()`：返回 this（类级方法，非接口成员）。

## 2. Java 用户如何使用
由 `AccessibleHTML.createElementInfo` 对 CONTENT/CAPTION 创建 `TextElementInfo`，其 `getAccessibleContext()` 惰性创建本类。文档中可能含多个文本元素（前后换行 + 正文），需按元素跨度/光标位置定位目标上下文。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/AccessibleHTML.java`：`class TextElementInfo`（第 824 行附近）+ `class TextAccessibleContext`（第 843 行附近）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleText.java`：接口定义。

## 4. 测试用例设计（黑盒部分，`TextAccessibleContextTest.testBlackBox`）
1. 收集所有 TextAccessibleContext，按 `getCharCount()==元素跨度 && getCaretPosition()==editor.getCaretPosition()` 定位 "hello world" 上下文。
2. 角色 TEXT、name null（无 title）、description `"text/html"`。
3. `getCharCount()==元素跨度`；caret/selectionStart/End 委托给编辑器；无选中 `getSelectedText()==null`。
4. `getCharacterAttribute(start)` 非 null；`getAtIndex(CHARACTER, start)=="h"`、`start+1=="e"`；越界/负数 → null。
5. 反射 `getAccessibleText()==this`。
