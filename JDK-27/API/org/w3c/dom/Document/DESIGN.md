# Document — 设计案

## 1. API 功能
DOM Document 接口（通过 DocumentBuilder 产出实例）。createElement/createElementNS/createTextNode/createComment/createCDATASection/createProcessingInstruction/createAttribute(NS)/createDocumentFragment/createEntityReference；appendChild 建树；getElementsByTagName(NS)；importNode/adoptNode；getDocumentElement/getDoctype/getImplementation/getXmlVersion 等。

## 2. Java 用户如何使用
newDocument() 或 parse() 后 createElement/appendChild 建树遍历。

## 3. 开源代码实际用法调研
JDK test/jdk/org/w3c/dom 用 DOM 建树、导入/收养节点断言 ownerDocument。

## 4. 测试用例设计 + 自动化实现
黑盒：createElement null/空/数字开头/空格→INVALID_CHARACTER_ERR；createElementNS(null,prefix)→NAMESPACE_ERR、空 ns→无命名空间；createAttribute(null)/createProcessingInstruction(null target)→INVALID_CHARACTER_ERR；appendChild(null)→NPE；树父子关系与 ownerDocument；getElementsByTagName(NS) 数量与通配；importNode/adoptNode 改 ownerDocument；parsed 文档 getXmlVersion/getXmlStandalone/getDoctype/getImplementation.hasFeature。
