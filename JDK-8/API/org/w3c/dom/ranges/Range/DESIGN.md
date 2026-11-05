# org.w3c.dom.ranges.Range — 设计案

## 1. API 功能
org.w3c.dom.ranges.Range 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；createRange 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Range）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 RangeTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 选点：setStart/setEnd 越界（-1 / 超过 childCount+1）→ `INDEX_SIZE_ERR(1)`；selectNode 边界=父节点内该节点两侧、公共祖先=父；selectNode(未挂载节点) → `RangeException INVALID_NODE_TYPE_ERR(2)`，selectNodeContents(未挂载) 得空 range。
- compareBoundaryPoints 四方向：相等 0、x 早 -1、x 晚 1、START_TO_END 1、END_TO_START -1。
- 三分差异（核心闭环）：cloneContents 拷贝子树且原树不动；extractContents 把覆盖节点移入 DocumentFragment（mid.getParentNode()==fragment，宿主剩 2 子）；deleteContents 直接移除并使 range 折叠。
- surroundContents 把内容重挂到新父节点且内容保序；setStartBefore/setEndAfter 边界落在父节点；collapse(true/false) 均得折叠点；cloneRange 与原 range 互不影响。
- 生命周期：detach 后 toString → `INVALID_STATE_ERR(11)`；Xerces 双重 detach 同样抛 `INVALID_STATE_ERR(11)`（双侧一致，非幂等）。
