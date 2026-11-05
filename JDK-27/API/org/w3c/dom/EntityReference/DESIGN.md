# org.w3c.dom.EntityReference — 设计案

## 1. API 功能
org.w3c.dom.EntityReference 接口。标记接口，无声明成员。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.EntityReference() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.EntityReference）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EntityReferenceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EntityReferenceTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
expandEntityReferences 对比闭环：默认 true → 引用被替换为文本（r 子=1 Text、textContent="v1"）；false → 树中出现 EntityReference 节点（nodeType 5、nodeName=实体名、children=0 不展开）。工厂：createEntityReference(name) 合法（nodeType 5、无父、children 0、nodeValue null）、**不校验实体是否声明**（"undeclared" 也成功）；('a b')/(null) → 5。readonly：appendChild → 7。可正常挂入内容、clone/import 保名；未展开引用对 textContent 无贡献。
