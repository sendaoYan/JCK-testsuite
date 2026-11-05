# XmlAttachmentRef 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlAttachmentRef`（标记注解）SwaRef 附件引用映射：把属性标记为"按引用的 SOAP 附件"（WS-I Attachment Profile 1.0 的 swaRef 类型），marshal 时 JAXB 向 `AttachmentMarshaller.addSwaRefAttachment(DataHandler)` 索取 URI 并把该 URI 写进文档。典型落点是 `javax.activation.DataHandler` 属性。

## 2. 使用方式

```java
@XmlAttachmentRef @XmlElement DataHandler att;
// marshal 时配合
m.setAttachmentMarshaller(new AttachmentMarshaller() {
    public String addSwaRefAttachment(DataHandler d) { return "cid:swaref-1"; }
    public String addMtomAttachment(DataHandler d, String ns, String ln) { return null; }
    public String addMtomAttachment(byte[] b, int o, int l, String mt, String ns, String ln) { return null; }
});
```

本测试闭环：设置返回 `cid:swaref-1` 的 AttachmentMarshaller → marshal 输出 `<att>cid:swaref-1</att>` 且回调恰好 1 次；负例：不设 AttachmentMarshaller 直接 marshal → `MarshalException`。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAttachmentRef.html
- JSR 222 (JAXB 2.2) 第 8.8 节、WS-I AP 1.0
- OpenJDK 8u RI 实测；本仓库测试 `XmlAttachmentRefTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD, PARAMETER})` 恰 3 处（含 PARAMETER）、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：字段/方法/参数三目标；未标注 null；跨类实例 equals。
3. marshal 闭环：回调次数恰 1、URI 出现在元素文本。
4. 负例：无 AttachmentMarshaller → MarshalException。

易错规范细节：

- **`AttachmentMarshaller` 是抽象类有 3 个抽象方法**：两个 `addMtomAttachment` 重载 + `addSwaRefAttachment(DataHandler)`，`isXOPPackage()` 有默认实现——匿名实现类必须给全 3 个。
- JDK-8 RI 输出形态是 `<att>cid:swaref-1</att>`（URI 作元素文本），**不是** `href` 属性——不要按 WS-I 文档例子断言 href。
- `new DataHandler("cid:part1", "application/octet-stream")` 把首参当 **URL 名**（`getName()` 返回 null），别断言回调收到的 name。
- 标到 `byte[]` 上在运行期 `ClassCastException`（byte[] 不能转 DataHandler）包成 MarshalException；本测试采用 DataHandler+无 marshaller 路径锁负例。
