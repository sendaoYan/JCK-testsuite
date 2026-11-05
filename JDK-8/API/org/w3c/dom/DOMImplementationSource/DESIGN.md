# DOMImplementationSource — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMImplementationSource javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document.getImplementation() 或解析 DTD 文档获得实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
JDK 内该接口的唯一活实现是 Xerces 内部 DOMImplementationSourceImpl（java.xml 未导出，非公开）；行为测试双轨：(1) registry 作为对外门面 —— getDOMImplementation("Core 3.0"/"XML 3.0"/"LS 3.0") 非 null 且满足 feature、CSS/HTML/bogus null、null → NPE、组合 feature 串 "Core 3.0 LS 3.0" 命中、含未知 token 失败、list(0) == 单查结果；(2) 自建 Source 门面（匿名实现）验证接口路由契约：两方法一致返回、不匹配 null/空表。
