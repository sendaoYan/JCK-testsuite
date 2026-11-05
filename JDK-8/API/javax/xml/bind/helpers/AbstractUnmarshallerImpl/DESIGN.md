# javax.xml.bind.helpers.AbstractUnmarshallerImpl 契约覆盖设计（JDK-8）

## 1. API 功能
Unmarshaller 的部分实现基类：把 7 种输入形态（InputSource/StreamSource/SAXSource/Reader/InputStream/File/URL）全部汇聚到唯一的抽象方法 `unmarshal(XMLReader, InputSource)`（模板方法群多为 final），并管理事件处理器与 validating 标志。**注意：基类没有实现接口的 `unmarshal(Node)` 与 `getUnmarshallerHandler()`，具体子类必须一并实现**；JAXB 2.0 新增面全部抛 UnsupportedOperationException。

## 2. 使用方式
```java
class MyUnmarshaller extends AbstractUnmarshallerImpl {
    protected Object unmarshal(XMLReader r, InputSource s) throws JAXBException { ... }
    public Object unmarshal(Node n) { ... }            // 接口要求，基类未实现
    public UnmarshallerHandler getUnmarshallerHandler() { ... }
}
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/AbstractUnmarshallerImpl.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/AbstractUnmarshallerImpl.java`
- 本仓库测试：`AbstractUnmarshallerImplTest.java`

## 4. 测试用例设计 + 易错规范细节
- 漏斗契约（实测锁定）：7 种输入形态全部恰好一次进入抽象方法；基类供给非 null XMLReader；File/URL 形态把 `file:` URL 写入 InputSource.systemId（与 `tmp.toUri().toURL().toExternalForm()` 精确比对）；Reader/InputSource 形态 systemId 为 null。
- null 检查矩阵：InputSource/Source/File/Reader/InputStream 为 null → IAE（"source parameter must not be null" 等）。
- 子类抛出的 JAXBException **原样传播（同实例，不二次包装）**。
- validating：默认 false；`setValidating(true)` 后 protected 字段 `validating` 同步为 true（经子类包装读出）。
- 属性：set/get 一律 PropertyException（与 Marshaller 基类不同——这里没有已知属性白名单）。
- 事件处理器：默认 DefaultValidationEventHandler；set(null) 恢复默认。
- 2.0 面 12 个操作全部 UnsupportedOperationException：get/setSchema、get/setAttachmentUnmarshaller、get/setListener、setAdapter/getAdapter、unmarshal(XMLEventReader/XMLStreamWriter/Node+Class/Source+Class)——实测锁定。
- `createUnmarshalException(SAXException)`：返回 UnmarshalException 且 linked==cause==入参（protected，经子类调用）。
- 易错点：只实现抽象方法不实现接口两缺口会编译失败；临时文件 finally 删除。
