# DocumentEvent.ElementChange 行为测试设计

## 1. API 功能
`javax.swing.event.DocumentEvent$ElementChange` 是文档元素变更描述接口（`DocumentEvent` 的嵌套接口）。

- **查询**：`getElement()`（被变更的元素）、`getIndex()`（子元素变更位置）、`getChildrenRemoved()`（被移除子元素数组）、`getChildrenAdded()`（被添加子元素数组）。

## 2. Java 用户如何使用
在 `DocumentListener` 回调中，通过 `DocumentEvent.getChange(element)` 取得后读取结构变更：

```java
DocumentEvent.ElementChange change = e.getChange(doc.getDefaultRootElement());
Element element = change.getElement();
int index = change.getIndex();
Element[] removed = change.getChildrenRemoved();
Element[] added = change.getChildrenAdded();
```

每个方法典型调用：`getElement()` 定位变更元素 → `getIndex()` 定位位置 → `getChildrenAdded()/getChildrenRemoved()` 对比增删内容（`Arrays.equals` 前后快照）。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java` 的 `DefaultDocumentEvent` 内部实现 `getChange(Element)` 并返回 `ElementChange`，是 Swing 文本结构更新的权威实现。
2. `javax.swing.text` 包中 `ElementChange` 的数组语义（`getChildrenAdded/Removed`）被 `DocumentListener` 用户用于增量刷新 UI。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（接口、public、无父类、无接口）。
- 黑盒：匿名实现 `ElementChange`，用 `PlainDocument.getDefaultRootElement()` 作为 element，验证 `getElement/getIndex/getChildrenRemoved/getChildrenAdded` 的 round-trip、数组长度与元素身份，覆盖「removed 1 个、added 2 个」的非对称组合。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
