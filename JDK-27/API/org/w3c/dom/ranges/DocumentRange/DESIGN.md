# org.w3c.dom.ranges.DocumentRange — 设计案

## 1. API 功能
org.w3c.dom.ranges.DocumentRange 接口。实例/声明方法：createRange。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ranges.DocumentRange() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ranges.DocumentRange）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DocumentRangeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentRangeTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`(DocumentRange) doc` 直接转型（Xerces Document 实现 DocumentRange）。
- createRange：新 range collapsed、锚在 Document 自身、偏移 0/0；每次返回独立对象（改动互不影响）；容器不随后续 append 变化；第二个文档产出自己的 range。
