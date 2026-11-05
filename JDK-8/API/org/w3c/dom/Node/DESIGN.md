# Node — 设计案

## 1. API 功能
DOM Node 接口（通过 Document 实例化各节点类型）。节点类型常量；getNodeName/getNodeValue/setNodeValue/getNodeType；树操作 appendChild/insertBefore/replaceChild/removeChild；cloneNode/normalize/getTextContent/setTextContent；compareDocumentPosition/isSameNode/isEqualNode；命名空间 getNamespaceURI/getPrefix/getLocalName/lookupNamespaceURI/lookupPrefix/isDefaultNamespace。

## 2. Java 用户如何使用
Document.createElement 建树后调用 Node 的树操作/克隆/规范化/文本内容/位置比较。

## 3. 开源代码实际用法调研
JDK test/jdk/org/w3c/dom 与 JCK 校验节点类型、树操作、clone/normalize/textContent、compareDocumentPosition。

## 4. 测试用例设计 + 自动化实现
黑盒：12 个节点类型常量值；各节点类型 nodeName/nodeValue；setNodeValue 对 element 无操作；树操作返回旧/被插入/被移除节点与父子兄弟关系；removeChild(非子)→NOT_FOUND_ERR、appendChild(null)→NPE；childNodes 活对象；cloneNode 浅/深；normalize 合并文本；getTextContent/setTextContent；compareDocumentPosition 的 CONTAINED_BY/CONTAINS/FOLLOWING 位；isSameNode/isEqualNode；命名空间直接属性与 setPrefix；parsed 文档 lookupNamespaceURI/lookupPrefix/isDefaultNamespace。

## 5. 深化第 2 轮（契约级，2026-11-05）
新增 deepContract()：
- DOCUMENT_POSITION 六常量精确值；实测位掩码：祖先→后代=20(FOLLOWING|CONTAINED_BY)、反向=10、前兄弟 vs 后兄弟=4/2、自身=0、不连通 37/35（DISCONNECTED|IMPLEMENTATION_SPECIFIC|方向位）。
- appendChild 移动语义（树内节点 append 到队尾、计数不变）；环/自插 → HIERARCHY_REQUEST_ERR(3)；insertBefore(x,x)/replaceChild(x,x) 合法 no-op；replaceChild 树内新节点=先移后换；removeChild(documentElement) 后 getDocumentElement null 可重挂；insertBefore/appendChild(null)→NPE；missing ref→NOT_FOUND_ERR(8)；Text 插子→3；跨 doc→WRONG_DOCUMENT_ERR(4)。
- cloneNode：浅拷保留属性、parent null、ownerDocument 不变；深拷子节点全新实例；无 handler 不复制 userData。
- 相等：isEqualNode(null)→NPE；isSameNode(null)==false；跨文档同构 true；文本/属性差异 false。
- normalize 吸收空 Text 且幂等；textContent 矩阵（comment/PI/attr/空 doc=null、setTextContent(null) 清子、Document.setTextContent no-op）。
- userData：前值返回、k=null 清除、CLONED/IMPORTED/RENAMED/ADOPTED 四回调 op/key/data/src/dst 全参、adopt 后 data 随行、操作码常量 1..5。
- 命名空间 lookup 矩阵（默认 ns、null 前缀→默认、Xerces lookupNamespaceURI("xml") 未声明时 null——实测锁定）；getBaseURI 继承 documentURI；isSupported；setPrefix 负例（非法字符→5、非 ns→14）；importNode attr/comment/fragment。
易错：两侧行为一致；NPE 只断言类型（消息不同）；全部断言经双侧探针实测。
