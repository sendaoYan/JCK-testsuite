# org.w3c.dom.DOMConfiguration — 设计案

## 1. API 功能
org.w3c.dom.DOMConfiguration 接口。实例/声明方法：setParameter, getParameter, canSetParameter, getParameterNames。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DOMConfiguration() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DOMConfiguration）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMConfigurationTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMConfigurationTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
参数契约：默认值（comments/entities/well-formed/split-cdata-sections true、canonical-form/validate false、error-handler null）；canSetParameter 矩阵（comments 双向 true、错值类型 false、element-content-whitespace=false 不可）；getParameter/setParameter 未知名→NOT_FOUND_ERR(8)、错值类型→8（Xerces 复用码，消息 FEATURE_NOT_FOUND）；getParameterNames() 返回**活 DOMStringList**（实为 Xerces DOMStringListImpl，≥15 项，contains("comments") 等命中）。normalizeDocument 行为：comments=false 删注释且合并相邻 Text；cdata-sections=false 把 CDATA 转为 Text；默认参数也做 Text 合并。LSParser.getDomConfig() 与文档配置是不同实例。
