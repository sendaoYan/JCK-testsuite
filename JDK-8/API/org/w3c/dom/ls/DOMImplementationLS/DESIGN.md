# org.w3c.dom.ls.DOMImplementationLS — 设计案

## 1. API 功能
DOMImplementationLS 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`impl.getFeature("LS","3.0")` 强转 DOMImplementationLS。
- 常量：MODE_SYNCHRONOUS=1、MODE_ASYNCHRONOUS=2。
- 工厂矩阵：createLSParser(sync, null) 同步解析器（async=false）；schema-type 传 "http://www.w3.org/2001/XMLSchema" 被接受；createLSParser(async) → `DOMException NOT_SUPPORTED_ERR(9)`；createLSSerializer/createLSInput/createLSOutput 均非空且每次新对象。