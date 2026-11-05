# javax.xml.bind.util.JAXBResult 契约覆盖设计（JDK-8）

## 1. API 功能
TRaX `Result` 适配器（extends SAXResult）：把 Transformer 的 SAX 输出事件直接泵入 JAXB Unmarshaller，实现"XML 变换 → JAXB 绑定"零中转闭环。两个构造器（JAXBContext / Unmarshaller），`getResult()` 取绑定结果对象。

## 2. 使用方式
```java
JAXBResult r = new JAXBResult(JAXBContext.newInstance(Person.class));
Transformer t = TransformerFactory.newInstance().newTransformer();
t.transform(new StreamSource(new StringReader(xml)), r);  // SAX 事件直通 unmarshaller
Person p = (Person) r.getResult();
```
真实闭环（本测试实证）：变换后 getResult() 返回 Person，name/age 全等；**变换前 getResult() 抛 IllegalStateException**。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/util/JAXBResult.html
- JSR 222 (JAXB 2.2) 规范第 4 章
- OpenJDK 8u RI 源码 `javax/xml/bind/util/JAXBResult.java`
- 本仓库测试：`JAXBResultTest.java`

## 4. 测试用例设计 + 易错规范细节
- 结构：extends SAXResult、is-a javax.xml.transform.Result、2 个公共构造器、systemId 默认 null 且 set/getSystemId 继承可用。
- **时序契约（实测锁定）**：未变换先调 getResult() → IllegalStateException（非 JAXBException）。
- Transformer 真实闭环：ctx 构造与 unmarshaller 构造两条路都完成 round-trip（字段全等）；getResult() 重复调用返回**同一实例**（稳定，锁定）；第二个 JAXBResult 实例独立可用。
- 易错点：Transformer 用 identity（newTransformer() 无参）即可；坏词法数据（age 非数字）经该通道可能不抛——那是 UnmarshallerHandler 的异步消费语义，不在本目录断言。
