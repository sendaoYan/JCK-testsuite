# Comment — 设计案

## 1. API 功能
（见 org.w3c.dom.Comment javadoc / DOM Level 3 Core）

## 2. 使用方式
Document 工厂创建实例后按 CharacterData/Node 语义操作。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
身份：nodeName "#comment"、nodeType 8、instanceof CharacterData；接口无 splitText（反射断言）；内容原样保留（含首尾空格）；setData/setNodeValue/setTextContent 三通道 + append/insert/delete/replaceData 全可用、substringData(99,1)→INDEX_SIZE_ERR(1)；createComment(null)→null data + getLength NPE（类型断言）+ setData 恢复；叶子节点（无子、getAttributes null）；可挂元素内、可挂文档级（root 之前）且可移除；isEqualNode 比较数据、clone 保数据；解析保留注释且对 textContent 聚合不可见。
