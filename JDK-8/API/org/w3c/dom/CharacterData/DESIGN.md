# CharacterData — 设计案

## 1. API 功能
（见 org.w3c.dom.CharacterData javadoc / DOM Level 3 Core）

## 2. 使用方式
Document 工厂创建实例后按 CharacterData/Node 语义操作。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
行为化（createTextNode/createComment 实例）：getData/getNodeValue/getTextContent/getLength 四读同步；setData/setNodeValue/setTextContent 三写通道互相同步；setData(null)/createTextNode(null)/createComment(null) → data 为 null 且后续 getLength 抛 NPE（消息双侧不同，仅断言类型），setData 可恢复；appendData(null) 无操作。
编辑边界矩阵（实测 Xerces）：substringData(4,2)/(0,6) 越 count 收敛、substringData(6,1)/(len,0)/(0,-1)/(-1,2) → INDEX_SIZE_ERR(1)——注意 offset==len 拒绝但 offset+count>len 允许；insertData(len,..) 追加、(6,..)→1；deleteData 越界 count 收敛、(len,0) no-op、(6,0)→1；replaceData 缩放/追加/(7,1,..)→1。编辑不改父节点 childCount，textContent 实时可见。Comment 同享 CharacterData 通道（nodeName "#comment"）。
