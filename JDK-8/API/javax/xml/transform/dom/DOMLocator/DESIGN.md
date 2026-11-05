# javax.xml.transform.dom.DOMLocator — 设计案

## 1. API 功能
带 Originating-Node 语义的 SourceLocator 子接口:`public Node getOriginatingNode()`。继承 getPublicId/getSystemId/getLineNumber/getColumnNumber 四个定位方法。

## 2. Java 用户如何使用
实现该接口,在 XSLT 错误回调中给出错误发生的 DOM 节点;或作为 SourceLocator 传给 TransformerException 构造器复用定位信息。

## 3. 开源代码实际用法调研
DOMResult/DOMLocator 在 JAXP DOM 输出错误定位路径中定义;实际 JDK 实现很少直接实例化,主要作为用户自定义定位器契约。

## 4. 测试用例设计 + 易错细节
白盒:是 interface、非空、方法集 = SourceLocator 4 方法 + getOriginatingNode(反射精确比对)。
黑盒:匿名实现携带 Document.createElement("origin") 节点;getOriginatingNode() 同一引用;作为 SourceLocator 塞进 TransformerException 后 getMessage()/getLocator() 往返一致。
易错:getLineNumber/getColumnNumber 返回 -1 表示"未知",自定义实现应返回 -1 而非 0(与 SAX Locator 惯例对齐)。
