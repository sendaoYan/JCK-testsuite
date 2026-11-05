# javax.xml.stream.events.EndElement — 设计案

## 1. API 功能
结束元素事件接口（extends XMLEvent）。方法：getName（QName）、getNamespaces（Iterator<Namespace>）。

## 2. Java 用户如何使用
解析路径：XMLEventReader 逐事件推进到 isEndElement() 后 asEndElement()；工厂路径：
XMLEventFactory.createEndElement(QName, Iterator ns)/(prefix, nsURI, local)/(prefix, nsURI, local, Iterator ns)。

## 3. 开源代码实际用法调研
JDK 实现 EndElementEvent；JDK test/jdk javax/xml/stream；本仓库 EndElementTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 解析路径：`<p:r xmlns...>x</p:r>` 的 END_ELEMENT 事件码 2、getName 三元组精确、asEndElement 返回自身；
  JDK 实现解析事件的 getNamespaces() 恒为空迭代器（即使该元素声明过命名空间，out-of-scope 信息不保留 —— impl quirk）。
- 工厂路径：三种重载 getName 均精确；传入的 ns 迭代器被实现吞掉（getNamespaces 空 —— 与解析路径一致的 quirk）。
- writeAsEncodedUnicode：createEndElement("p","http://p","local") 输出 `</p:local>`（与开始元素的括号形态不对称）。
- 嵌套文档 `<r><c/></r>` 的事件序含内层 END c → 外层 END r → END_DOCUMENT。
- 错误窄化 asCharacters/asStartElement → CCE；getSchemaType 为 null。
