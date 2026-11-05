# org.w3c.dom.ls.DOMImplementationLS — 设计案

## 1. API 功能
org.w3c.dom.ls.DOMImplementationLS 接口。常量：MODE_SYNCHRONOUS, MODE_ASYNCHRONOUS（共 2 个）。 实例/声明方法：createLSParser, createLSSerializer, createLSInput, createLSOutput。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ls.DOMImplementationLS() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ls.DOMImplementationLS）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMImplementationLSTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMImplementationLSTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`impl.getFeature("LS","3.0")` 强转 DOMImplementationLS。
- 常量：MODE_SYNCHRONOUS=1、MODE_ASYNCHRONOUS=2。
- 工厂矩阵：createLSParser(sync, null) 同步解析器（async=false）；schema-type 传 "http://www.w3.org/2001/XMLSchema" 被接受；createLSParser(async) → `DOMException NOT_SUPPORTED_ERR(9)`；createLSSerializer/createLSInput/createLSOutput 均非空且每次新对象。
