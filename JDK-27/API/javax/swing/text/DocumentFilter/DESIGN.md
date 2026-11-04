# javax.swing.text.DocumentFilter — DESIGN

## 1. API 功能
Document 变更过滤器。当 Document 执行 insert/remove/replace 时，若设置了 DocumentFilter，则先转发到过滤器，过滤器可放行、改写或拒绝。方法分组：
- 构造器：`DocumentFilter()`。
- 过滤钩子：`insertString(FilterBypass,int,String,AttributeSet)`、`remove(FilterBypass,int,int)`、`replace(FilterBypass,int,int,String,AttributeSet)`；默认实现直接调用 FilterBypass 放行。

## 2. Java 用户如何使用
`AbstractDocument doc = (AbstractDocument) area.getDocument(); doc.setDocumentFilter(new DocumentFilter(){ ... 覆写 insertString/remove/replace ... })`；钩子内调用 `super.xxx(fb, ...)` 放行或直接操作 `fb`（FilterBypass）改写；`doc.insertString/remove/replace` 会自动进入过滤器。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java`：`DocumentFilter filter = getDocumentFilter(); if (filter != null) { filter.insertString(getFilterBypass(), offs, str, a); } else { handleInsertString(offs, str, a); }` —— 有过滤器则转发，无则直插。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JPasswordField.java`：`DocumentFilter filter = null; ... filter = adoc.getDocumentFilter();` —— 密码框读取/重置过滤器。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultFormatter.java`：`private class DefaultDocumentFilter extends DocumentFilter ...` 覆写 replace 并 `fb.replace(...)`，是格式化输入限制的实现载体。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java`（DefaultFilterBypass）：`handleRemove`/`handleInsertString` 绕开过滤器，避免回调死循环。

## 4. 测试用例设计（黑盒部分，`DocumentFilterTest.testBlackBox`）
1. 初始无过滤器；setDocumentFilter/getDocumentFilter 往返。
2. 自定义过滤器覆写 insertString/remove/replace 并记录调用：insertString 转大写、remove/replace 放行，断言文档文本与调用记录。
3. 过滤器收到的 FilterBypass.getDocument() 与文档同一。
4. 直接通过 FilterBypass 的 insertString/remove/replace 变更文档，验证不重新进入过滤器（调用计数不变）。
5. 取消过滤器后 getDocumentFilter 为 null。
