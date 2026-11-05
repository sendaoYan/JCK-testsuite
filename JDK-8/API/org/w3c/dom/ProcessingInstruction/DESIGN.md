# ProcessingInstruction — 设计案

## 1. API 功能
（见 org.w3c.dom.ProcessingInstruction javadoc / DOM Level 3 Core）

## 2. 使用方式
Document 工厂创建实例后按 CharacterData/Node 语义操作。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
身份：target 即 getNodeName、nodeType 7、data 即 getNodeValue/getTextContent；setData 与 nodeValue/textContent 双向同步；setData(null)/create(target,null) → null data；目标名合法性：'a b'/''/'a<b' → INVALID_CHARACTER_ERR(5)，而 **'xml'/'xmL'/'xml-stylesheet' 在 API 层均允许**（实测双侧一致）；叶子结构（无子、getAttributes null）；clone 保 target+data 且独立、importNode 重绑 owner；可挂文档级（root 之前）；isEqualNode 比较 target+data；解析侧 target 与 data 在首个空白处切分（`<?xml-stylesheet href='x.css'?>` → data "href='x.css'"）。
