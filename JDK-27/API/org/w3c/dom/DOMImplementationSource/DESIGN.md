# org.w3c.dom.DOMImplementationSource — 设计案

## 1. API 功能
org.w3c.dom.DOMImplementationSource 接口。实例/声明方法：getDOMImplementation, getDOMImplementationList。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DOMImplementationSource() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DOMImplementationSource）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMImplementationSourceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMImplementationSourceTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
JDK 内该接口的唯一活实现是 Xerces 内部 DOMImplementationSourceImpl（java.xml 未导出，非公开）；行为测试双轨：(1) registry 作为对外门面 —— getDOMImplementation("Core 3.0"/"XML 3.0"/"LS 3.0") 非 null 且满足 feature、CSS/HTML/bogus null、null → NPE、组合 feature 串 "Core 3.0 LS 3.0" 命中、含未知 token 失败、list(0) == 单查结果；(2) 自建 Source 门面（匿名实现）验证接口路由契约：两方法一致返回、不匹配 null/空表。
