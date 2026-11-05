# XmlInlineBinaryData 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlInlineBinaryData`（标记注解）禁用该属性的多部分编码（XOP/MTOM 或 SwaRef），强制二进制数据**内联 base64**进 XML 文档，即使marshaller 配置了AttachmentMarshaller 也不外提附件。目标是精细控制哪些二进制属性走附件、哪些内联。

## 2. 使用方式

```java
@XmlRootElement(name="inlineBin") @XmlAccessorType(XmlAccessType.FIELD)
static class InlineBin {
    @XmlInlineBinaryData byte[] blob;   // <blob>AwT/</blob>
}
```

本测试闭环：`{3,4,0xff}` marshal 成 `<blob>AwT/</blob>`（base64 文本子元素，无 cid: 引用），再与 `DatatypeConverter.printBase64Binary` 交叉核对字面量；unmarshal round-trip 字节全等（含高字节 0xff 无符号还原）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlInlineBinaryData.html
- JSR 222 (JAXB 2.2) 第 8.8.2 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlInlineBinaryDataTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD, TYPE})` 恰 3 处——**含 TYPE，是本批次里仅有的两个 TYPE 目标字段注解之一**（另一个 XmlTransient）、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：字段级/TYPE 级/方法级/未标注/TYPE 级不隐式下放到字段（负向）/跨类 equals/重复获取。
3. marshal/unmarshal 闭环：base64 字面量精确、无 cid:、字节精确 round-trip。

易错规范细节：

- **TYPE 级标注不会隐式作用于字段**：类级 `@XmlInlineBinaryData` 后字段回读仍为 null（实测）——语义只是"类内默认"，回读面必须分开断言。
- base64 字面量容易手算错（`{3,4,0xff}` → `AwT/` 而非 `AwT+`），测试用 `DatatypeConverter.printBase64Binary` 双保险。
- 无 AttachmentMarshaller 时 byte[] 本来就内联；注解的效果要在"设置了 MTOM marshaller + isXOPPackage()==true"的对照下才分叉——本测试锁定"内联恒成立 + 无 cid:"这一可确定面。
- round-trip 后 Java 字节是有符号的，高字节比较要 `& 0xff`。
