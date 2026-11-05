# org.w3c.dom.traversal.TreeWalker — 设计案

## 1. API 功能
org.w3c.dom.traversal.TreeWalker 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；createTreeWalker 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Traversal）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 TreeWalkerTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 导航矩阵：firstChild/lastChild/nextSibling/previousSibling/parentNode/currentNode(get/set)/getRoot；越出根边界（parentNode/nextSibling/previousSibling）返回 null 不抛，之后仍可正常走。
- setCurrentNode 可跳到子树任意位置并从那里继续导航；lastChild 下降到“当前节点”的最后子节点（不是兄弟的最后子节点）。
- SKIP vs REJECT（walker 核心差异）：全 SKIP 零产出但候选序列含全部后代（子树保留）；全 REJECT 候选只有顶层元素（子树剪枝）；mixed（只 REJECT 元素 a）产出 b+其文本、a 的文本永不呈现。
- SHOW_ATTRIBUTE 的 walker 在普通树上零产出（属性非树节点）。
