# NameList — 设计案

## 1. API 功能
（见 org.w3c.dom.NameList javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
JDK 无活生产者（参数名表是 DOMStringList 不是 NameList）——降级为接口反射签名（getName/getNamespaceURI/getLength/contains/containsNS）+ 用户实现行为锁定（索引序、ns 可 null、越界 null、containsNS 双参命中）。
