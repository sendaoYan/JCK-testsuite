# javax.xml.transform.dom.DOMResult — 设计案

## 1. API 功能
javax.xml.transform.dom.DOMResult 类。常量：FEATURE（共 1 个）。 实例/声明方法：setNode, getNode, setNextSibling, getNextSibling, setSystemId, getSystemId。

## 2. Java 用户如何使用
new DOMResult(doc/elem) → getNode/getNextSibling/getSystemId。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.dom.DOMResult）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMResultTest.testBlackBox()`）覆盖：三种构造 + systemId。

## 5. 深化（本轮新增）
- 构造矩阵 5 种 round-trip + FEATURE 精确值。
- 负例（消息关键词锁定）：nextSibling 不是 node 后代 → IAE "nextSibling is not contained by the node"；nextSibling 非空时 setNode(null) → ISE（须先 setNextSibling(null)）。
- 真实闭环：Stream→DOMResult 后 getNode() 为 DOCUMENT_NODE、根名/子节点数/属性逐项断言。
- 易错：两元素必须先建立父子关系再构造 DOMResult。
