# javax.xml.bind.PropertyException 契约覆盖设计（JDK-8）

## 1. API 功能
`Marshaller.setProperty/getProperty` / `Unmarshaller.setProperty/getProperty` 遇到未知名或非法值时抛出的受检异常，继承 `JAXBException`；比父类多一个 `(String name, Object value)` 便捷构造，message 直接携带属性名与值。

## 2. 使用方式
```java
try {
    m.setProperty("bogus", v);
} catch (PropertyException pe) {
    // message == "name: bogus value: v"（RI 锁定格式）
}
```
本测试实证的 RI 通道：Marshaller 与 Unmarshaller 的 set/get 四个入口对未知属性名全部抛本类型。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/PropertyException.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `javax/xml/bind/PropertyException.java`
- 本仓库测试：`PropertyExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵 6 形态（5 个继承形态 + `(name,value)` 专属形态）。
- **(name,value) message 精确锁定 `"name: propX value: valX"`**（空格与顺序均不可变）。
- 真实通道：JAXBContext(Person) 后 Marshaller.setProperty("bogus.property")/getProperty/Unmarshaller.setProperty/getProperty 四路负例全部命中 PropertyException。
- serialVersionUID 计算值 `4091278398208222475`。
- setLinkedException 与 getCause 同步（继承 JAXBException）。
- 易错点：AbstractMarshallerImpl（helpers）与 RI 真实 MarshallerImpl 对 `JAXB_ENCODING` 非法值（非 String）也抛 PropertyException，message 为 "jaxb.encoding must be a String"——那是 helpers 契约，本目录只测异常类型本身。
