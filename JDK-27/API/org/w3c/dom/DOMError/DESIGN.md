# org.w3c.dom.DOMError — 设计案

## 1. API 功能
org.w3c.dom.DOMError 接口。常量：SEVERITY_WARNING, SEVERITY_ERROR, SEVERITY_FATAL_ERROR（共 3 个）。 实例/声明方法：getSeverity, getMessage, getType, getRelatedException, getRelatedData, getLocation。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DOMError() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DOMError）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMErrorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMErrorTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
真实实例经 LSParser.getDomConfig().setParameter("error-handler",h) + parse 坏 XML 取得：severity=SEVERITY_FATAL_ERROR(3)、message 非空、type 以 "wf-" 开头、getRelatedException 非 null、getLocation 非 null；SEVERITY_WARNING=1/SEVERITY_ERROR=2 常量。handler 返回 true 或 false 都终止于 LSException PARSE_ERR(81)（code 是公有字段无 getter）；良构文档零回调。
