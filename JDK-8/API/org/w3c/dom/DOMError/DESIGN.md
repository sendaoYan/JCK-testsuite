# DOMError — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMError javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
真实实例经 LSParser.getDomConfig().setParameter("error-handler",h) + parse 坏 XML 取得：severity=SEVERITY_FATAL_ERROR(3)、message 非空、type 以 "wf-" 开头、getRelatedException 非 null、getLocation 非 null；SEVERITY_WARNING=1/SEVERITY_ERROR=2 常量。handler 返回 true 或 false 都终止于 LSException PARSE_ERR(81)（code 是公有字段无 getter）；良构文档零回调。
