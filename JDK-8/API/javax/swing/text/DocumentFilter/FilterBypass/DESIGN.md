# javax.swing.text.DocumentFilter.FilterBypass — DESIGN

## 1. API 功能
DocumentFilter 的回调旁路（抽象类），用于在过滤器内部直接变更 Document 而不重新触发过滤器。方法分组：
- 构造器：`protected FilterBypass()`。
- 查询：`getDocument()` 返回被变更的 Document。
- 变更：`remove(int,int)`、`insertString(int,String,AttributeSet)`、`replace(int,int,String,AttributeSet)`（均可能抛 `BadLocationException`）。

## 2. Java 用户如何使用
在 `DocumentFilter` 子类的方法签名里拿到 `FilterBypass fb`：`fb.getDocument()` 拿到文档；`fb.insertString(off, text, attr)` 直接插入（绕过过滤器）；`super.insertString(fb, ...)` 等价放行。默认 `DocumentFilter` 的方法内部就是调用 `fb.xxx`。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/AbstractDocument.java`：`private class DefaultFilterBypass extends DocumentFilter.FilterBypass { public Document getDocument() { return AbstractDocument.this; } public void remove(int offset, int length) { handleRemove(offset, length); } ... }` —— JDK 内部实现直接调用 handle* 方法绕开过滤器。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/DocumentFilter.java`：`public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) { fb.insertString(offset, string, attr); }` —— 默认放行就是调用旁路。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/DefaultFormatter.java`（`ReplaceHolder`）：`DocumentFilter.FilterBypass fb;` —— 格式化器在 replace 流程中持有旁路引用。

## 4. 测试用例设计 + 自动化实现
（15 断言）：抽象类形状（getDocument/remove/insertString/replace）；实现具体 bypass（getDocument 返回目标文档）后 insertString/remove/replace 驱动文档精确生效（replace 需将 Document 转型 AbstractDocument 调用）。
