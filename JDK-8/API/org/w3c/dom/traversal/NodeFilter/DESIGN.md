# org.w3c.dom.traversal.NodeFilter — 设计案

## 1. API 功能
org.w3c.dom.traversal.NodeFilter 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；createNodeIterator/createTreeWalker 注入 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Traversal）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 NodeFilterTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量表：FILTER_ACCEPT=1、FILTER_REJECT=2、FILTER_SKIP=3；SHOW_* 全 16 位掩码（ELEMENT=0x1 … NOTATION=0x800）；SHOW_ALL 为 int -1（0xFFFFFFFF，十六进制校验）。
- 用户过滤器对每个候选节点各咨询一次、按文档序；Xerces walker 在走完末节点寻找下一兄弟时会回头再咨询一次根（实测序列 a、#text、r，双侧一致）。
- whatToShow 先于 filter 生效：SHOW_ELEMENT 时文本候选根本不进 filter，但 root 仍作为候选呈现。
- 决策语义（核心差异）：TreeWalker 上 SKIP 跳过节点保留子树、REJECT 剪掉整个子树。
