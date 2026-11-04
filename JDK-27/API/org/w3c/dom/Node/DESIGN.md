# Node — 设计案

## 1. API 功能
DOM Node 接口（通过 Document 实例化各节点类型）。节点类型常量；getNodeName/getNodeValue/setNodeValue/getNodeType；树操作 appendChild/insertBefore/replaceChild/removeChild；cloneNode/normalize/getTextContent/setTextContent；compareDocumentPosition/isSameNode/isEqualNode；命名空间 getNamespaceURI/getPrefix/getLocalName/lookupNamespaceURI/lookupPrefix/isDefaultNamespace。

## 2. Java 用户如何使用
Document.createElement 建树后调用 Node 的树操作/克隆/规范化/文本内容/位置比较。

## 3. 开源代码实际用法调研
JDK test/jdk/org/w3c/dom 与 JCK 校验节点类型、树操作、clone/normalize/textContent、compareDocumentPosition。

## 4. 测试用例设计 + 自动化实现
黑盒：12 个节点类型常量值；各节点类型 nodeName/nodeValue；setNodeValue 对 element 无操作；树操作返回旧/被插入/被移除节点与父子兄弟关系；removeChild(非子)→NOT_FOUND_ERR、appendChild(null)→NPE；childNodes 活对象；cloneNode 浅/深；normalize 合并文本；getTextContent/setTextContent；compareDocumentPosition 的 CONTAINED_BY/CONTAINS/FOLLOWING 位；isSameNode/isEqualNode；命名空间直接属性与 setPrefix；parsed 文档 lookupNamespaceURI/lookupPrefix/isDefaultNamespace。
