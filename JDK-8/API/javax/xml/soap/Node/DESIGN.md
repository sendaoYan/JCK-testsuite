# javax.xml.soap.Node 契约测试设计

## 1. API 功能

`javax.xml.soap.Node`（**注意不是 org.w3c.dom.Node**；`extends org.w3c.dom.Node`，6 个自有方法）是 SAAJ 在 DOM 之上的树管理协议：`getValue/setValue`（值语义）、`setParentElement/getParentElement`（SAAJ 父链，比 DOM getParentNode 更"知道"SOAP 结构）、`detachNode`（摘除）、`recycleNode`（回收提示）。所有 SAAJ 元素/文本/注释实现都实现本接口。

## 2. 使用方式

真实通道：`SOAPBodyElement el = body.addBodyElement(qname)`（本身就是 SAAJ Node）→ `el.setValue("v")`/`getValue()` → `el.detachNode()`（body 计数归零）→ `el.setParentElement(body)`（RI 会真实重新收养回 DOM 子列表）→ `el.recycleNode()`（等价 detach + 节点仍可用）。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/Node.html
- JSR 67 (SAAJ 1.3)
- OpenJDK 8u RI：`com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl`（detachNode/recycleNode/getValue 实现）
- 本仓库测试：`JDK-8/API/javax/xml/soap/Node/NodeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：值生命周期（fresh null → setValue 单文本子 → 覆盖仍单子）→ detach 生命周期（双 detach/摘除后可用）→ setParentElement 再收养 → recycleNode 语义 → header 通道复用 → SAAJ/DOM 双视角 → setValue(null) 负例。

易错点（实测锁定）：

1. **`getValue()` 有读取归并副作用（normalize-on-read）**：`setValue("v2") + addTextNode("x")` 后子节点为 2 个 Text；一旦调用元素级 `getValue()`，相邻文本被**归并为 1 个**（值 "v2x"）。**先迭代计数再 getValue**，顺序不能反。
2. `setValue` 是**替换语义**：连续两次 setValue 子数恒为 1；`addTextNode` 则追加兄弟 Text 节点。
3. `setValue(null)` 抛 **NullPointerException**（不是清空值！清空用 `removeContents` 或 detach 文本子）。
4. **detach 后 `setParentElement(parent)` 会真实重新挂回 DOM 子列表**（RI 实测 body 计数 0→1）——不是仅改字段。
5. `recycleNode()` 等价 detach（父清空、计数减一），**幂等不抛**，且节点仍可继续 setValue 使用（RI 不失效化）。
6. `setParentElement` 返回 **void**（不是 this）。
7. 元素的 `getOwnerDocument()` 是 RI 内部 `SOAPDocumentImpl`，**不 instanceof SOAPPart**——断言用 `org.w3c.dom.Document` 层次即可。
8. detach 后 `getParentElement()` 与 `getParentNode()` 均 null（两套父链同步清）。
