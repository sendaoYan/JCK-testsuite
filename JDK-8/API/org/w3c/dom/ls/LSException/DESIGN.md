# org.w3c.dom.ls.LSException — 设计案

## 1. API 功能
LSException 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量：PARSE_ERR=81、SERIALIZE_ERR=82；code 为 public 字段（无 getCode），可变；RuntimeException 子类；null message 保留。
- 真实触发：无 error-handler 的 LSParser 解析 `<r><1x/></r>` → `LSException PARSE_ERR(81)`。