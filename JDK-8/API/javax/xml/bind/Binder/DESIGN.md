# Binder 设计案（JDK-8 契约测试）

## 1. API 功能
就地（in-place）DOM 绑定：marshal(obj, xmlTree) 写入、unmarshal(xmlNode) 读出、updateXML(obj)/updateJAXB(node) 双向同步、getXMLNode/getJAXBNode 关联查询。

## 2. 使用方式
ctx.createBinder(Node.class)（DOM binder）。marshal 进空 Document 会附加根元素；unmarshal 后对象与节点建立关联。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- DOM 闭环：marshal→根元素 person + name/age 子元素；unmarshal(root)/unmarshal(doc)/unmarshal(root, Class) 全回读。
- 同步：updateXML(bound) **替换根节点**（旧引用失效，需重新 getDocumentElement——实测大坑）；updateJAXB(node) 拾取 DOM 修改。
- 关联：getXMLNode(绑定对象) 返回节点；getJAXBNode(未绑定节点) null；getXMLNode(未绑定对象) null。
- 属性/事件：5 标准属性读写、未知 → PropertyException；setEventHandler(null) **不还原同一默认实例**（RI 每次新装默认）。
- 易错点：createBinder(XMLStreamReader.class) 在 JDK-8 RI 抛 UnsupportedOperationException。
