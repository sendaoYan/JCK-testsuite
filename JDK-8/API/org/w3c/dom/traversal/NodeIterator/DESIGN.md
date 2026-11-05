# org.w3c.dom.traversal.NodeIterator — 设计案

## 1. API 功能
org.w3c.dom.traversal.NodeIterator 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；createNodeIterator 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Traversal）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 NodeIteratorTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 文档序平铺：nextNode 序列从 root 自身开始（root;a;#text;b;#text→null），耗尽后再 nextNode 恒 null。
- 边界语义：耗尽后 previousNode 回到最后一个节点、再 previousNode 到 b、随后 nextNode 复访 b（位置语义：previous 后 next 重新给出同一节点）；未做任何 next 前 previousNode → null。
- 活视图：迭代中后续 appendChild 对已创建的迭代器可见。
- SKIP vs REJECT：NodeIterator 上二者等价（平铺文档序照常深入子节点；候选序列相同 r,a,#text,b,#text）——与 TreeWalker 的 REJECT 剪枝形成对照。
- detach 幂等（不抛）；detach 后 nextNode → `DOMException INVALID_STATE_ERR(11)`，getRoot 仍返回 root。
