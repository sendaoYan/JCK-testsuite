# org.w3c.dom.ls.LSInput — 设计案

## 1. API 功能
LSInput 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 工厂 LSInput 全属性缺省 null/false，7 个属性 setter/getter 往返（stringData/systemId/publicId/baseURI/encoding/certifiedText）。
- 行为闭环：stringData 供 LSParser 消费建树；用户自实现 LSInput（characterStream 通道）同样可解析（duck typing）。