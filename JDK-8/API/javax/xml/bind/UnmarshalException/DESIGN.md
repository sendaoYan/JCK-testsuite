# javax.xml.bind.UnmarshalException 契约覆盖设计（JDK-8）

## 1. API 功能
`Unmarshaller.unmarshal` 操作失败时抛出的受检异常，直接继承 `JAXBException`，无新增方法；语义 = "反序列化（XML→对象）方向失败"。是日常 JAXB 数据通道中最常见的失败信号。

## 2. 使用方式
```java
try {
    Person p = (Person) u.unmarshal(new StringReader(xml));
} catch (UnmarshalException ue) {
    ue.getLinkedException(); // 坏 XML 时为 SAXParseException；根名不匹配时为 null
}
```
真实通道（本测试实证）：坏 XML `<broken` → UnmarshalException(linked=SAXParseException)；根名不匹配 `<notperson/>` → UnmarshalException(linked=null，**不是链式**)。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/UnmarshalException.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `javax/xml/bind/UnmarshalException.java`
- 本仓库测试：`UnmarshalExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵 5 形态全测 + setLinkedException 与 getCause 同步断言。
- serialVersionUID 计算值 `8222528164859488039`（ObjectStreamClass.lookup 锁定）。
- **真实通道矩阵（文件内 @XmlRootElement Person 嵌套类建上下文）**：
  - malformed XML → UnmarshalException，`getLinkedException() instanceof SAXParseException`，`getCause()==getLinkedException()`，消息在 linked 里（自身 getMessage 可为 null——实测锁定，勿断言非空）；
  - 根名不匹配 → UnmarshalException，linked/cause 均 null；
  - 好数据在多轮失败后仍可正常 round-trip（异常不污染上下文状态）。
- 易错点：malformed 场景断言 message 时要用 `getLinkedException().getMessage()`，不是 `getMessage()`。
