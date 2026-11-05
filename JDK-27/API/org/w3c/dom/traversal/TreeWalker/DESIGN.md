# org.w3c.dom.traversal.TreeWalker — 设计案

## 1. API 功能
org.w3c.dom.traversal.TreeWalker 接口。实例/声明方法：getRoot, getWhatToShow, getFilter, getExpandEntityReferences, getCurrentNode, setCurrentNode, parentNode, firstChild, lastChild, previousSibling, nextSibling, previousNode, nextNode。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.traversal.TreeWalker() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.traversal.TreeWalker）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TreeWalkerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TreeWalkerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 导航矩阵：firstChild/lastChild/nextSibling/previousSibling/parentNode/currentNode(get/set)/getRoot；越出根边界（parentNode/nextSibling/previousSibling）返回 null 不抛，之后仍可正常走。
- setCurrentNode 可跳到子树任意位置并从那里继续导航；lastChild 下降到“当前节点”的最后子节点（不是兄弟的最后子节点）。
- SKIP vs REJECT（walker 核心差异）：全 SKIP 零产出但候选序列含全部后代（子树保留）；全 REJECT 候选只有顶层元素（子树剪枝）；mixed（只 REJECT 元素 a）产出 b+其文本、a 的文本永不呈现。
- SHOW_ATTRIBUTE 的 walker 在普通树上零产出（属性非树节点）。
