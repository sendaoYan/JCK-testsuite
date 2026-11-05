# EntityReference — 设计案

## 1. API 功能
（见 org.w3c.dom.EntityReference javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document.getImplementation() 或解析 DTD 文档获得实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
expandEntityReferences 对比闭环：默认 true → 引用被替换为文本（r 子=1 Text、textContent="v1"）；false → 树中出现 EntityReference 节点（nodeType 5、nodeName=实体名、children=0 不展开）。工厂：createEntityReference(name) 合法（nodeType 5、无父、children 0、nodeValue null）、**不校验实体是否声明**（"undeclared" 也成功）；('a b')/(null) → 5。readonly：appendChild → 7。可正常挂入内容、clone/import 保名；未展开引用对 textContent 无贡献。
