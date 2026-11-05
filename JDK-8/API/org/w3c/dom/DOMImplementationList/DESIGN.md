# DOMImplementationList — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMImplementationList javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document.getImplementation() 或解析 DTD 文档获得实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
实例经 registry.getDOMImplementationList("Core 3.0")（JDK 无公开工厂）：length>=1（实测 4）、item(0) 非 null、item(99)/item(-1) null、全 in-range item 均可解析、列表项均满足查询 feature、重复查询结果确定、"bogus 1.0" → length 0。
