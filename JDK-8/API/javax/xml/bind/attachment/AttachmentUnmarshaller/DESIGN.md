# javax.xml.bind.attachment.AttachmentUnmarshaller 契约覆盖设计（JDK-8）

## 1. API 功能
MTOM/XOP 与 SwaRef 读方向附件查找的抽象基类：2 个抽象方法（getAttachmentAsDataHandler(contentId)、getAttachmentAsByteArray(contentId)）+ 1 个默认方法 **isXOPPackage() 返回 false**。由 Unmarshaller.setAttachmentUnmarshaller 注入，运行时解析 xop:Include / cid 引用时回调。

## 2. 使用方式
```java
class MyAu extends AttachmentUnmarshaller {
    public DataHandler getAttachmentAsDataHandler(String cid) { return store.get(cid); }
    public byte[] getAttachmentAsByteArray(String cid) { return bytes.get(cid); }
}
u.setAttachmentUnmarshaller(new MyAu());
u.unmarshal(source);   // 注入不破坏 unmarshal 闭环
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/attachment/AttachmentUnmarshaller.html
- JSR 222 (JAXB 2.2) 规范附件支持（MTOM/XOP, WSI Attachment Profile 1.0）
- OpenJDK 8u RI 源码 `javax/xml/bind/attachment/AttachmentUnmarshaller.java`
- 本仓库测试：`AttachmentUnmarshallerTest.java`

## 4. 测试用例设计 + 易错规范细节
- 抽象面反射锁定：3 个声明方法中恰 2 个 abstract（两个 get* 方法）、isXOPPackage 非 abstract；new 抽象类 InstantiationException。
- 默认行为：`isXOPPackage()` == false（锁定）。
- 回调语义（记录型子类直调）：getAttachmentAsByteArray("cid:x") 返回 {65,66,67}（长度+逐字节断言）；getAttachmentAsDataHandler 可返回 DataHandler 也可返回 **null**（合法，实测）；content-id 参数逐条记录。
- 真实注入（核心闭环）：RI Unmarshaller set/getAttachmentUnmarshaller 同实例回读；默认 null；装上后 unmarshal Person round-trip 字段全等；**isXOPPackage 每次 unmarshal 恰被查询 1 次（两次 unmarshal 计 2，锁定）**；纯文本内容不触发任何附件查找。
- 易错点：计数基线要算清——若测试前面已直调 isXOPPackage 一次，累计值从该次起算；xop:Include 真实解析属 annotation 域，此处只锁注入通道。
