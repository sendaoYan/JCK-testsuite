# DOMErrorHandler — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMErrorHandler javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
路由闭环：坏 XML → handleError 收 DOMError（severity 3）；返回 true → parse 仍抛 LSException(81)；返回 false → 同样 LSException(81)；良构输入 handler 静默。文档侧同一契约：normalizeDocument 对非 ns 元素树回调 type="NullLocalElementName"（severity ERROR）——非 NS createElement 建树必触发；createElementNS 建树零回调。
