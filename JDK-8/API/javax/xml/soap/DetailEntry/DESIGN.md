# DetailEntry — 设计案

## 1. API 功能
Detail 的条目（extends SOAPElement）。**纯标记接口——自身不声明任何方法**（getDeclaredMethods().length==0），全部能力继承自 SOAPElement/Node/DOM Element。

## 2. 使用方式
只能经 `Detail.addDetailEntry(...)` 产生；其上可用完整 SOAPElement 写通道（addTextNode/addChildElement/addAttribute/addNamespaceDeclaration/setValue/setEncodingStyle/removeContents/detachNode）。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：接口形态（零自有方法）、真实通道（fault→detail→entry 链路 + 双 entry 独立性 + QName 回读）、元素能力（文本/子元素/属性/命名空间声明）、Node 桥（getValue/setValue/节点类型 ELEMENT_NODE）、编码风格回读、removeContents、同名 entry 共存与独立、detach 减员。断言 30 条（旧 4）。
- 坑1：无公开构造——直接 new 不可能，必须走 detail 通道（与 Detail 的创建途径差异）。
- 坑2：同名 DetailEntry 可共存多条，各自文本独立。
- 坑3：removeContents 后迭代可能残留文本节点——计数断言放宽为 0 或 1。
