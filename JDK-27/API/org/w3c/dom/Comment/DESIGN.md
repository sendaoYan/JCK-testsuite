# org.w3c.dom.Comment — 设计案

## 1. API 功能
org.w3c.dom.Comment 接口。标记接口，无声明成员。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.Comment() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.Comment）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CommentTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CommentTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
身份：nodeName "#comment"、nodeType 8、instanceof CharacterData；接口无 splitText（反射断言）；内容原样保留（含首尾空格）；setData/setNodeValue/setTextContent 三通道 + append/insert/delete/replaceData 全可用、substringData(99,1)→INDEX_SIZE_ERR(1)；createComment(null)→null data + getLength NPE（类型断言）+ setData 恢复；叶子节点（无子、getAttributes null）；可挂元素内、可挂文档级（root 之前）且可移除；isEqualNode 比较数据、clone 保数据；解析保留注释且对 textContent 聚合不可见。
