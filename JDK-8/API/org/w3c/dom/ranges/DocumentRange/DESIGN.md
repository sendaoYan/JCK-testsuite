# org.w3c.dom.ranges.DocumentRange — 设计案

## 1. API 功能
org.w3c.dom.ranges.DocumentRange 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；(DocumentRange) doc 直接转型 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Range）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 DocumentRangeTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`(DocumentRange) doc` 直接转型（Xerces Document 实现 DocumentRange）。
- createRange：新 range collapsed、锚在 Document 自身、偏移 0/0；每次返回独立对象（改动互不影响）；容器不随后续 append 变化；第二个文档产出自己的 range。
