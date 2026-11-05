# Marshaller 设计案（JDK-8 契约测试）

## 1. API 功能
对象→XML 序列化器：marshal 多通道（Writer/OutputStream/Result/ContentHandler/Node）、属性系统（5 个标准属性常量）、Listener 生命周期回调、AttachmentMarshaller 注入、Adapter 注册、Schema 校验控制。

## 2. 使用方式
ctx.createMarshaller() → setProperty → marshal(obj, sink)。Listener 在 marshal 前后回调；附件 marshaller 在 MTOM/SwaRef 场景被查询。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 属性矩阵：5 常量串全等（jaxb.encoding/jaxb.formatted.output/jaxb.schemaLocation/jaxb.noNamespaceSchemaLocation/jaxb.fragment）；默认值锁定（formatted=false、encoding=UTF-8、schemaLocation 类=null）；未知属性读写 → PropertyException 且消息含属性名。
- Listener：before→after 顺序、各 >=1 次（RI 实际对同一对象回调 2 次——**勿断言精确次数**）、setListener(null) 移除回调、getListener 同一实例。
- 通道矩阵：Writer/StreamResult/OutputStream/DOMResult（根 localName==person）/ContentHandler（SAX startElement 序列）；marshal(null) → IAE；marshal(非映射对象) → JAXBException。
- 附件注入不破坏闭环：setAttachmentMarshaller 后 marshal 仍输出 person。
- Adapter：setAdapter(adapter) 以 adapter 自身 class 为键；getAdapter(类型) 默认 null。
- 易错点：Marshaller.Listener 是**抽象类**不是接口；AttachmentMarshaller 的抽象方法是 addMtomAttachment×2 + addSwaRefAttachment（签名带 DataHandler），isXOPPackage 有默认实现。
