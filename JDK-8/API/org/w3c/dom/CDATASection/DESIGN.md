# CDATASection — 设计案

## 1. API 功能
（见 org.w3c.dom.CDATASection javadoc / DOM Level 3 Core）

## 2. 使用方式
Document 工厂创建实例后按 CharacterData/Node 语义操作。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
身份：nodeName "#cdata-section"、nodeType 4、instanceof Text；CharacterData 编辑族全继承（append/substring/replace）；splitText 后**新半是普通 Text（type 3, #text）而非 CDATA**（split(0) 同理）；getWholeText 返回自身数据；replaceWholeText 归并后节点保持 CDATA 类型（与 Text 调用路径不同，实测）；createCDATASection("a]]>b") 合法（DOM 层不禁止）；解析侧：`<![CDATA[x < y & z]]>` 内容保持原始不转义、相邻实体引用文本照常展开、空 CDATA len 0；isEqualNode 同数据相等、CDATA 永不等于 Text。
