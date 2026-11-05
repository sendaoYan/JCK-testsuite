# org.w3c.dom.ProcessingInstruction — 设计案

## 1. API 功能
org.w3c.dom.ProcessingInstruction 接口。实例/声明方法：getTarget, getData, setData。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ProcessingInstruction() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ProcessingInstruction）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ProcessingInstructionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ProcessingInstructionTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
身份：target 即 getNodeName、nodeType 7、data 即 getNodeValue/getTextContent；setData 与 nodeValue/textContent 双向同步；setData(null)/create(target,null) → null data；目标名合法性：'a b'/''/'a<b' → INVALID_CHARACTER_ERR(5)，而 **'xml'/'xmL'/'xml-stylesheet' 在 API 层均允许**（实测双侧一致）；叶子结构（无子、getAttributes null）；clone 保 target+data 且独立、importNode 重绑 owner；可挂文档级（root 之前）；isEqualNode 比较 target+data；解析侧 target 与 data 在首个空白处切分（`<?xml-stylesheet href='x.css'?>` → data "href='x.css'"）。
