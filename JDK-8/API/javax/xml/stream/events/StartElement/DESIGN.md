# javax.xml.stream.events.StartElement — 设计案

## 1. API 功能
开始元素事件接口（extends XMLEvent）。方法：getName（QName ns/local/prefix）、getAttributes（Iterator<Attribute>）、
getNamespaces（Iterator<Namespace>）、getAttributeByName(QName)、getNamespaceContext()、getNamespaceURI(prefix)。

## 2. Java 用户如何使用
经 XMLEventReader 解析带命名空间/属性的 XML 后 nextEvent().asStartElement() 取真实事件；
或 XMLEventFactory.createStartElement(QName, Iterator attrs, Iterator namespaces)（另有 prefix/ns/local 三参、
五参、六参 + NamespaceContext 形态）手工构造。

## 3. 开源代码实际用法调研
JDK 实现 StartElementEvent；JDK test/jdk javax/xml/stream；本仓库 StartElementTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 纯无命名空间属性 `<r a b c d>`：getAttributes 迭代严格保持文档顺序 a,b,c,d；每项 value/文档类型 CDATA/isSpecified。
- 命名空间元素 `<p:r xmlns='http://d' xmlns:p='http://p' ...>`：getName 三元组精确；getNamespaceURI(p)/('')
  命中、未知前缀 null；getNamespaceContext 的 URI('')/URI(p)/prefix 反查/prefixes(uri) 计数；
  ctx.getNamespaceURI(null) → IllegalArgumentException。
- 混合属性（无 ns + 有 ns）：JDK 迭代顺序为「无命名空间属性按文档序在前，命名空间属性在后」（a,c,b，确定性）；
  断言该顺序并记录。
- getAttributes 迭代器 remove → UnsupportedOperationException；无属性/无命名空间元素迭代器 hasNext()==false。
- 未声明前缀的属性 `<r p:b='2'/>` → XMLStreamException，消息含 AttributePrefixUnbound。
- writeAsEncodedUnicode：解析事件输出 `<['http://p']:p:r a='1' c='3' p:b='2' xmlns:='http://d' xmlns:p='http://p'>`
  （实现用 ['ns']:prefix:local 括号形态、默认命名空间写成 xmlns:= —— 两个 impl quirk，双侧一致锁定）。
- 工厂 createStartElement：无命名空间属性按插入序；传入的 Namespace 迭代器不接入查询
  （getNamespaceURI(x)=null）且 getNamespaceContext()=null（impl quirk）；getAttributeByName 正常。
  createStartElement(prefix, ns, local) 编码为 `<['http://p']:p:local>`。
- 易错：createStartElement/createEndElement 三参形态是 (prefix, namespaceURI, localName)，不是 (ns, local, prefix)。
