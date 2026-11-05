# SOAPElement — 设计案

## 1. API 功能
SOAP 树节点统一接口（extends javax.xml.soap.Node + org.w3c.dom.Element）。写通道家族：addChildElement 五形态（local/local+prefix/local+prefix+uri/Name/QName/元素移动）、addTextNode、addAttribute(Name/QName)、addNamespaceDeclaration、setElementQName；读通道：getElementName/getElementQName/getAttributeValue/getAllAttributes(AsQNames)/getNamespaceURI/getNamespacePrefixes/getVisibleNamespacePrefixes/getChildElements(无参/QName/Name)；编码风格 setEncodingStyle/getEncodingStyle；removeContents/removeAttribute/removeNamespaceDeclaration。

## 2. 使用方式
`body.addChildElement(new QName(ns,local,pfx)).addTextNode(v)` 链式写；`el.addAttribute(qn, v)`+`el.getAttributeValue(qn)` 回读；`el.addNamespaceDeclaration("p",uri)`+`getNamespaceURI("p")`；过滤 `getChildElements(qname)` 计数。本测试在真实 envelope/body 树上逐族验证。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：五形态添加精确回读（Name 三元组 + QName 全等）、属性族（双通道/删除返回值/迭代器元素类型 Name 与 QName）、命名空间族（声明回读/删除/前缀枚举/createQName 前缀解析）、子元素过滤（无参含 Text 节点/QName/Name 过滤计数）、文本（两次 addTextNode 相连 + DOM textContent 一致 + setValue 替换）、编码风格回读、removeContents 清空、detachNode 父清空、移动/改名、负例（未绑定前缀）。断言 95 条（旧 25）。
- 坑1：`addChildElement(local, prefix)` 前缀未绑定时抛 SOAPException "Unable to locate namespace for prefix X"——先 addNamespaceDeclaration 才可用。
- 坑2：**无参 getChildElements() 迭代包含 Text 节点**（RI TextImpl），不是"仅元素"。
- 坑3：`addChildElement(SOAPElement)` 是**移动**语义（同一实例重挂新父），javadoc 描述为拷贝——实测锁定。
- 坑4：addAttribute 对未声明前缀的 QName（如 QName.valueOf("p:x")）**不抛**（RI 宽松）。
- 坑5：getAllAttributes 迭代元素是 Name 对象；getAllAttributesAsQNames 是 QName 对象。
- 坑6：getNamespacePrefixes 只看本元素声明，getVisibleNamespacePrefixes 含祖先；断言用 superset 关系。
- 坑7：DOM 侧属性按限定名/NS 查（getAttribute("qy:a1")/getAttributeNS(ns,"a1")），裸 local 名查不到。
