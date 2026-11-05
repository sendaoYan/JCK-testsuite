# javax.xml.transform.dom.DOMResult — 设计案

## 1. API 功能
以 DOM 树为输出的 Result 实现。常量 `FEATURE="http://javax.xml.transform.dom.DOMResult/feature"`。构造器:(Node)、(Node,String)、(Node,Node nextSibling)、(Node,Node nextSibling,String)、无参;方法 setNode/getNode、setNextSibling/getNextSibling、setSystemId/getSystemId。JDK-8 无 isEmpty()。

## 2. Java 用户如何使用
`DOMResult r = new DOMResult(); transformer.transform(src, r); r.getNode()` 拿到新 Document;或指定已有 Node 作为挂载点(配合 nextSibling 控制插入位置)。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 DOM 往返用例;javax.xml.transform.dom 内部校验 nextSibling 包含关系。

## 4. 测试用例设计 + 易错细节
黑盒:构造矩阵 5 种 round-trip(node/nextSibling/systemId 各自独立);FEATURE 常量精确值。
负例(消息关键词锁定):(Node,nextSibling,systemId) 中 nextSibling 不是 node 的后代 → IllegalArgumentException "Cannot create a DOMResult when the nextSibling is not contained by the node.";nextSibling 非空时 setNode(null) → IllegalStateException "nextSibling is contained by the \"null\" node"(必须先 setNextSibling(null) 再 setNode(null))。
真实闭环:Stream→DOMResult 后 getNode() 是 DOCUMENT_NODE、根名/3 个子节点/属性值逐项断言。
易错:createElement 出来的元素未 append 进文档树,nextSibling 包含校验即失败——两元素必须先建立父子关系。
