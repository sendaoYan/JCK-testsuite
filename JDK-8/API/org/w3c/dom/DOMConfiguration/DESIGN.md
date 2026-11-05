# DOMConfiguration — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMConfiguration javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
参数契约：默认值（comments/entities/well-formed/split-cdata-sections true、canonical-form/validate false、error-handler null）；canSetParameter 矩阵（comments 双向 true、错值类型 false、element-content-whitespace=false 不可）；getParameter/setParameter 未知名→NOT_FOUND_ERR(8)、错值类型→8（Xerces 复用码，消息 FEATURE_NOT_FOUND）；getParameterNames() 返回**活 DOMStringList**（实为 Xerces DOMStringListImpl，≥15 项，contains("comments") 等命中）。normalizeDocument 行为：comments=false 删注释且合并相邻 Text；cdata-sections=false 把 CDATA 转为 Text；默认参数也做 Text 合并。LSParser.getDomConfig() 与文档配置是不同实例。
