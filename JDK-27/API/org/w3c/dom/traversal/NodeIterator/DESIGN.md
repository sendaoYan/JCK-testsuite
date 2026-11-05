# org.w3c.dom.traversal.NodeIterator — 设计案

## 1. API 功能
org.w3c.dom.traversal.NodeIterator 接口。实例/声明方法：getRoot, getWhatToShow, getFilter, getExpandEntityReferences, nextNode, previousNode, detach。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.traversal.NodeIterator() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.traversal.NodeIterator）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NodeIteratorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NodeIteratorTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 文档序平铺：nextNode 序列从 root 自身开始（root;a;#text;b;#text→null），耗尽后再 nextNode 恒 null。
- 边界语义：耗尽后 previousNode 回到最后一个节点、再 previousNode 到 b、随后 nextNode 复访 b（位置语义：previous 后 next 重新给出同一节点）；未做任何 next 前 previousNode → null。
- 活视图：迭代中后续 appendChild 对已创建的迭代器可见。
- SKIP vs REJECT：NodeIterator 上二者等价（平铺文档序照常深入子节点；候选序列相同 r,a,#text,b,#text）——与 TreeWalker 的 REJECT 剪枝形成对照。
- detach 幂等（不抛）；detach 后 nextNode → `DOMException INVALID_STATE_ERR(11)`，getRoot 仍返回 root。
