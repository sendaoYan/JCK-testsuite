# javax.xml.parsers.DocumentBuilder — 设计案

## 1. API 功能
DOM 解析器抽象类，由 DocumentBuilderFactory 产出。核心操作 `parse(InputStream/File/String/InputSource[, systemId]) → Document`；`newDocument()` 空文档；`getDOMImplementation()`；`reset()` 清空 handler；`setErrorHandler/setEntityResolver`；查询 `isNamespaceAware/isValidating/getSchema/isXIncludeAware`。

## 2. Java 用户如何使用
```java
DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
Document d = b.parse(new ByteArrayInputStream("<catalog>...".getBytes("UTF-8")));
NodeList nl = d.getElementsByTagName("book");
```
组合链路：`newDocumentBuilder → parse(固定 XML) → document 结构遍历`；`newDocument → createElement`；`parse(InputSource)` 与带 systemId 重载。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/xml/jaxp` 大量使用 `DocumentBuilder.parse(new InputSource(new StringReader(...)))` 解析内联 XML 并断言 DOM 结构。
- Spring `DomUtils`、`DocumentLoader`：`builder.parse(inputSource)` 后遍历 `getElementsByTagName` 提取 Bean 定义。
- 本仓库既有白盒断言覆盖 parse 重载与配置查询。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentBuilderTest.testBlackBox()`）：固定 XML `<catalog><book id="b1" lang="en"><title>T1</title></book></catalog>`，走 `parse(InputStream)` 断根元素名、`getElementsByTagName("book")` 数量、属性值、嵌套 textContent；`parse(InputSource)` 与 `parse(InputStream, systemId)` 两重载；`newDocument` 空文档可用 + `createElement`；`getDOMImplementation` 非空；默认配置查询；畸形 XML（`<a><b></a>`）抛 `SAXParseException` 且 line/column ≥ 1；`reset` 后再次 parse 成功。
