# XmlMimeType 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlMimeType` 携带属性的 MIME 类型提示（如 `"image/jpeg"`），供 schema 生成（`xmime:expectedContentTypes`）与 MTOM/SwaRef 附件编码选择优化路径。单成员 `value()` 返回 MIME 串，**无默认值**（必须显式给）。

## 2. 使用方式

```java
@XmlMimeType("application/octet-stream") byte[] blob;
@XmlMimeType("image/png") DataHandler picture;
```

本测试闭环：`{1,2}` marshal 成 `<blob>AQI=</blob>`（无 AttachmentMarshaller 时内联 base64、无 cid:），round-trip 字节全等。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlMimeType.html
- JSR 222 (JAXB 2.2) 第 8.8.1 节、W3C XML-binary Optimized Packaging
- OpenJDK 8u RI 实测；本仓库测试 `XmlMimeTypeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD, PARAMETER})` 恰 3 处（含 PARAMETER）、无 Inherited/Documented/Repeatable。
2. 成员集：恰 1 个 `value()` 返回 String、abstract、**`getDefaultValue()==null`**。
3. 回读矩阵：两个不同 MIME 字符串的等值性分叉（equals/hashCode 都要不等）、方法级/参数级/未标注/TYPE-null/跨类/重复获取。
4. marshal/unmarshal 闭环：base64 内联 + 无附件引用。

易错规范细节：

- **`value()` 是必填成员**（无默认）：源码里 `@XmlMimeType` 裸写会编译错；反射面用 `getDefaultValue() == null` 锁定（区别于 `""` 默认的注解成员）。
- 不同 value 的注解实例 `equals()` 为 false、`hashCode()` 不同——JDK 注解动态代理按成员值实现。
- MIME 串只是提示：**不设置 AttachmentMarshaller 时 byte[] 照样内联 base64**，注解不改变无附件 marshal 的形态（实测）。
- 该注解不参与映射决策，标在 DataHandler 上与 byte[] 上回读行为一致；闭环选 byte[]（避免 DataHandler 内容比较的麻烦）。
