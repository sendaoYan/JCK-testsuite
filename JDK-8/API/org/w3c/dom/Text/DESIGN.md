# Text — 设计案

## 1. API 功能
（见 org.w3c.dom.Text javadoc / DOM Level 3 Core）

## 2. 使用方式
Document 工厂创建实例后按 CharacterData/Node 语义操作。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
splitText 全矩阵：中点切分（原节点留前缀、新节点接后缀、父 childCount+1、新节点紧邻原节点之后、同 owner）；splitText(len)→空兄弟、splitText(0)→原节点清空、(99)/(-1)→INDEX_SIZE_ERR(1)；无父 splitText 也成功（新半无父）。
wholeText 家族：text+CDATA+text 相邻 → getWholeText 跨越拼接 "abc"；注释打断相邻性只返回本段；replaceWholeText("N") 归并为单一 Text 且返回原节点、replaceWholeText("")/null → 返回 null 且节点脱挂（Xerces）；normalize 只合并 Text 段、CDATA 作分隔保留（t/cdata/t/t → 3 子）；isElementContentWhitespace 未验证时恒 false；跨文档 isEqualNode 仅比较数据。
