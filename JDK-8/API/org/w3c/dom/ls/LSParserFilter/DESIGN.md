# org.w3c.dom.ls.LSParserFilter — 设计案

## 1. API 功能
LSParserFilter 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量：FILTER_ACCEPT=1、FILTER_REJECT=2、FILTER_SKIP=3、FILTER_INTERRUPT=4（注意与 NodeFilter 的 1/2/3 不同序！）。
- 行为闭环：ACCEPT 保留全部节点（acceptNode 咨询文本与元素）；REJECT 把过滤子节点全部剪掉（根保留）；INTERRUPT 提前停止构建（空根）；getWhatToShow=SHOW_ELEMENT 时文本节点不进 acceptNode。