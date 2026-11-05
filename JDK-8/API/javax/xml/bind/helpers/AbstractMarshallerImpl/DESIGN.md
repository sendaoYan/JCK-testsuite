# javax.xml.bind.helpers.AbstractMarshallerImpl 契约覆盖设计（JDK-8）

## 1. API 功能
Marshaller 的部分实现基类，供 JAXB 提供方/自定义 marshaller 复用：实现 5 个便捷 marshal 重载（模板方法，final）+ 事件处理器管理 + JAXB_* 属性的读写骨架。子类只需实现接口核心 `marshal(Object, Result)`。**JDK-8 RI 中 JAXB 2.0 新增（schema/adapter/listener/attachment/StAX）一律抛 UnsupportedOperationException。**

## 2. 使用方式
```java
class MyMarshaller extends AbstractMarshallerImpl {
    public void marshal(Object o, Result r) throws JAXBException { ... }
}
MyMarshaller m = new MyMarshaller();
m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");   // 基类存储
m.marshal(obj, outputStream);                        // final 便捷重载 → marshal(obj, StreamResult)
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/AbstractMarshallerImpl.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/AbstractMarshallerImpl.java`
- 本仓库测试：`AbstractMarshallerImplTest.java`

## 4. 测试用例设计 + 易错规范细节
- 抽象性：不可实例化（InstantiationException）；子类只补 `marshal(Object,Result)` 即具体化。
- 属性矩阵（基类默认行为契约）：JAXB_ENCODING 默认 "UTF-8"、FORMATTED_OUTPUT/FRAGMENT 默认 false，五属性 set/get 回读；未知属性 set/get 均 PropertyException；`setProperty(null)` → IAE "name parameter must not be null"；`JAXB_ENCODING=非 String` → PropertyException。
- 便捷重载委托矩阵（实测锁定）：OutputStream/Writer/File → StreamResult、ContentHandler → SAXResult、Node → DOMResult，全部落到子类 `marshal(Object,Result)`，计数逐一核对；`marshal(Object,OutputStream)` 为 final。
- null 检查：os/writer/node/contentHandler/obj 为 null → IAE（message 形如 "os parameter must not be null"）。
- 事件处理器：默认 DefaultValidationEventHandler；set 后同实例回读；set(null) 恢复为**新** DefaultValidationEventHandler 实例。
- 2.0 面 11 个操作全部 UnsupportedOperationException（get/setSchema、get/setListener、get/setAttachmentMarshaller、get/setAdapter、getNode、marshal(XMLEventWriter/XMLStreamWriter)）——实测锁定。
- protected 访问器（经子类包装方法）：getEncoding/isFormattedOutput/isFragment/getSchemaLocation/getNoNSSchemaLocation 与 JAXB_* 属性同步；`getJavaEncoding("UTF-8")`→"UTF-8"、未知名 → UnsupportedEncodingException。
- 易错点：marshal(File) 用后要删临时文件；不要在子类里再覆写 final 便捷方法。
