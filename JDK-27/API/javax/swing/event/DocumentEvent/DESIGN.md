# DocumentEvent 行为测试设计

## 1. API 功能
`javax.swing.event.DocumentEvent` 是文本文档变更事件接口，供 `Document` 实现派发。

- **查询**：`getOffset()`（变更起始偏移）、`getLength()`（变更长度）、`getDocument()`（所属 Document）、`getType()`（返回 `EventType`）、`getChange(Element)`（返回给定元素上的 `ElementChange`）。

## 2. Java 用户如何使用
用户实现 `DocumentListener` 读取变更信息：

```java
document.addDocumentListener(new DocumentListener() {
    public void insertUpdate(DocumentEvent e) {
        int off = e.getOffset();
        int len = e.getLength();
        EventType t = e.getType();               // INSERT
        Document doc = e.getDocument();
        ElementChange change = e.getChange(doc.getDefaultRootElement());
    }
    public void removeUpdate(DocumentEvent e) { }
    public void changedUpdate(DocumentEvent e) { }
});
```

每个方法典型调用：`getType()` 与 `EventType.INSERT/REMOVE/CHANGE` 比较 → 依类型调用 `getOffset()/getLength()` 读取范围 → `getDocument()` 回读文本 → `getChange(element)` 取得子元素增删。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:578/631/751`：
   ```java
   new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE);
   new DefaultDocumentEvent(offs, str.length(), DocumentEvent.EventType.INSERT);
   ```
   `DefaultDocumentEvent` 是 `DocumentEvent` 的典型实现。
2. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:1489` 同样构造 `DefaultDocumentEvent` 携带类型与偏移/长度。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（接口、public、无父类、无接口）。
- 黑盒：匿名实现 `DocumentEvent`，用 `PlainDocument` 作为 Document、`INSERT` 作为 EventType，配合匿名 `ElementChange`，验证 `getOffset/getLength/getDocument/getType` round-trip 与 `getChange` 按元素匹配/不匹配返回正确结果，形成「事件 → 文档 → 类型 → 元素变更」组合链。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
