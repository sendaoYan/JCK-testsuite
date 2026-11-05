# Document — 设计案

## 1. API 功能
DOM Document 接口（通过 DocumentBuilder 产出实例）。createElement/createElementNS/createTextNode/createComment/createCDATASection/createProcessingInstruction/createAttribute(NS)/createDocumentFragment/createEntityReference；appendChild 建树；getElementsByTagName(NS)；importNode/adoptNode；getDocumentElement/getDoctype/getImplementation/getXmlVersion 等。

## 2. Java 用户如何使用
newDocument() 或 parse() 后 createElement/appendChild 建树遍历。

## 3. 开源代码实际用法调研
JDK test/jdk/org/w3c/dom 用 DOM 建树、导入/收养节点断言 ownerDocument。

## 4. 测试用例设计 + 自动化实现
黑盒：createElement null/空/数字开头/空格→INVALID_CHARACTER_ERR；createElementNS(null,prefix)→NAMESPACE_ERR、空 ns→无命名空间；createAttribute(null)/createProcessingInstruction(null target)→INVALID_CHARACTER_ERR；appendChild(null)→NPE；树父子关系与 ownerDocument；getElementsByTagName(NS) 数量与通配；importNode/adoptNode 改 ownerDocument；parsed 文档 getXmlVersion/getXmlStandalone/getDoctype/getImplementation.hasFeature。

## 深化第 2 轮（契约级，2026-11-05）
renameNode 全矩阵：返回同节点、ns 三元组更新、(nsEl,null,plain) 清 ns、attr 改名后仍挂在元素（新名可取）、text → NOT_SUPPORTED_ERR(9)、外文档节点 → WRONG_DOCUMENT_ERR(4)、非法名 → 5。adoptNode vs importNode 对照：adopt 原地转移所有权（同实例）、原文档 documentElement 变 null、**目标文档不自动安装为新根**（浮置）、幂等；import(false) 丢子保属性、import(true) 深拷且绝不扰动源；adopt/import(doctype) → 9。声明状态：xmlVersion 1.1 往返、9.9 → 9、standalone/strictErrorChecking/documentURI 往返、getDomConfig 非空；带声明的 parse：getXmlEncoding/getInputEncoding "UTF-8"。document.cloneNode(true) 拷根**不拷 doctype**（Xerces）。doctype 可作 root 前的文档子节点；无 DTD 时 getElementById null。
