# javax.xml.bind.attachment.AttachmentMarshaller 契约覆盖设计（JDK-8）

## 1. API 功能
MTOM/XOP 与 SwaRef 写方向附件回调的抽象基类：3 个抽象方法（addMtomAttachment(DataHandler,String,String)、addMtomAttachment(byte[],int,int,String,String,String)、addSwaRefAttachment(DataHandler)——返回 content-id 字符串）+ 1 个默认方法 **isXOPPackage() 返回 false**。由 Marshaller.setAttachmentMarshaller 注入，运行时在需要把二进制外置为 MIME 部件时回调。

## 2. 使用方式
```java
class MyAm extends AttachmentMarshaller {
    public String addMtomAttachment(DataHandler d, String ns, String ln) { return "cid:1"; }
    public String addMtomAttachment(byte[] b, int o, int l, String mt, String ns, String ln) { return "cid:2"; }
    public String addSwaRefAttachment(DataHandler d) { return "cid:3"; }
}
m.setAttachmentMarshaller(new MyAm());
m.marshal(obj, writer);   // 注入不破坏 marshal 闭环
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/attachment/AttachmentMarshaller.html
- JSR 222 (JAXB 2.2) 规范附件支持（MTOM/XOP, WSI Attachment Profile 1.0）
- OpenJDK 8u RI 源码 `javax/xml/bind/attachment/AttachmentMarshaller.java`
- 本仓库测试：`AttachmentMarshallerTest.java`

## 4. 测试用例设计 + 易错规范细节
- 抽象面反射锁定：4 个声明方法中恰 3 个 abstract（两个 addMtomAttachment 重载 + addSwaRefAttachment）、isXOPPackage 非 abstract；new 抽象类 InstantiationException。
- 默认行为：`isXOPPackage()` == false（锁定）。
- 回调语义（记录型子类直调）：三方法返回的 content-id 回读；DataHandler 参数同实例透传；byte[] 形态的 offset/length/mime/ns/ln 参数逐一记录比对；null content-id 返回合法（调用方将内联数据）。
- 真实注入（核心闭环）：RI Marshaller set/getAttachmentMarshaller 同实例回读；默认 null；装上后 marshal Person 完成、输出 token 完整（`<person>`/`<name>`/`<age>`）；**isXOPPackage 每次 marshal 恰被查询 1 次（两次 marshal 计 2，锁定）**；纯文本内容不触发任何 add* 回调。
- 易错点：查询次数断言必须按"每次 marshal 恰一次"累计；@XmlAttachmentRef/MTOM 真实外置属 annotation 域，此处只锁注入通道。
