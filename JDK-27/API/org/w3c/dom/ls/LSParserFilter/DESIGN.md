# org.w3c.dom.ls.LSParserFilter — 设计案

## 1. API 功能
org.w3c.dom.ls.LSParserFilter 接口。常量：FILTER_ACCEPT, FILTER_REJECT, FILTER_SKIP, FILTER_INTERRUPT（共 4 个）。 实例/声明方法：startElement, acceptNode, getWhatToShow。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ls.LSParserFilter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ls.LSParserFilter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 LSParserFilterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`LSParserFilterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 常量：FILTER_ACCEPT=1、FILTER_REJECT=2、FILTER_SKIP=3、FILTER_INTERRUPT=4（注意与 NodeFilter 的 1/2/3 不同序！）。
- 行为闭环：ACCEPT 保留全部节点（acceptNode 咨询文本与元素）；REJECT 把过滤子节点全部剪掉（根保留）；INTERRUPT 提前停止构建（空根）；getWhatToShow=SHOW_ELEMENT 时文本节点不进 acceptNode。
