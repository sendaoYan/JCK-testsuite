# org.w3c.dom.Text — 设计案

## 1. API 功能
org.w3c.dom.Text 接口。实例/声明方法：splitText, isElementContentWhitespace, getWholeText, replaceWholeText。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.Text() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.Text）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TextTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TextTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
splitText 全矩阵：中点切分（原节点留前缀、新节点接后缀、父 childCount+1、新节点紧邻原节点之后、同 owner）；splitText(len)→空兄弟、splitText(0)→原节点清空、(99)/(-1)→INDEX_SIZE_ERR(1)；无父 splitText 也成功（新半无父）。
wholeText 家族：text+CDATA+text 相邻 → getWholeText 跨越拼接 "abc"；注释打断相邻性只返回本段；replaceWholeText("N") 归并为单一 Text 且返回原节点、replaceWholeText("")/null → 返回 null 且节点脱挂（Xerces）；normalize 只合并 Text 段、CDATA 作分隔保留（t/cdata/t/t → 3 子）；isElementContentWhitespace 未验证时恒 false；跨文档 isEqualNode 仅比较数据。
