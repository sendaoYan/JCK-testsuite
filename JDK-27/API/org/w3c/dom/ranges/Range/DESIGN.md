# org.w3c.dom.ranges.Range — 设计案

## 1. API 功能
org.w3c.dom.ranges.Range 接口。常量：START_TO_START, START_TO_END, END_TO_END, END_TO_START（共 4 个）。 实例/声明方法：getStartContainer, getStartOffset, getEndContainer, getEndOffset, getCollapsed, getCommonAncestorContainer, setStart, setEnd, setStartBefore, setStartAfter, setEndBefore, setEndAfter, collapse, selectNode, selectNodeContents, compareBoundaryPoints, deleteContents, extractContents, cloneContents, insertNode, surroundContents, cloneRange, toString, detach。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ranges.Range() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ranges.Range）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 RangeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`RangeTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 选点：setStart/setEnd 越界（-1 / 超过 childCount+1）→ `INDEX_SIZE_ERR(1)`；selectNode 边界=父节点内该节点两侧、公共祖先=父；selectNode(未挂载节点) → `RangeException INVALID_NODE_TYPE_ERR(2)`，selectNodeContents(未挂载) 得空 range。
- compareBoundaryPoints 四方向：相等 0、x 早 -1、x 晚 1、START_TO_END 1、END_TO_START -1。
- 三分差异（核心闭环）：cloneContents 拷贝子树且原树不动；extractContents 把覆盖节点移入 DocumentFragment（mid.getParentNode()==fragment，宿主剩 2 子）；deleteContents 直接移除并使 range 折叠。
- surroundContents 把内容重挂到新父节点且内容保序；setStartBefore/setEndAfter 边界落在父节点；collapse(true/false) 均得折叠点；cloneRange 与原 range 互不影响。
- 生命周期：detach 后 toString → `INVALID_STATE_ERR(11)`；Xerces 双重 detach 同样抛 `INVALID_STATE_ERR(11)`（双侧一致，非幂等）。
