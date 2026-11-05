# org.w3c.dom.ls.LSOutput — 设计案

## 1. API 功能
LSOutput 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 工厂 LSOutput 缺省 null，systemId/encoding/byteStream/characterStream 往返。
- 行为闭环：LSSerializer.write(doc, LSOutput) 向接入的 characterStream 写出，writeToString 与之正文一致。