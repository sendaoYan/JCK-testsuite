# javax.xml.bind.util.JAXBSource 契约覆盖设计（JDK-8）

## 1. API 功能
TRaX `Source` 适配器（extends SAXSource）：内部持有一个 Marshaller，被 Transformer 消费时按需把绑定对象 marshal 成 SAX 事件——"JAXB 绑定 → XML 变换"零中转闭环。两个构造器（JAXBContext / Marshaller，均含内容对象）。

## 2. 使用方式
```java
JAXBSource s = new JAXBSource(ctx, person);
Transformer t = TransformerFactory.newInstance().newTransformer();
t.transform(s, new StreamResult(writer));   // marshal 通道输出
```
真实闭环（本测试实证）：输出含 `<?xml ...?>`/`<person>`/`<name>alice</name>`/`<age>30</age>`；**同一 JAXBSource 可被消费第二次**。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/util/JAXBSource.html
- JSR 222 (JAXB 2.2) 规范第 4 章
- OpenJDK 8u RI 源码 `javax/xml/bind/util/JAXBSource.java`
- 本仓库测试：`JAXBSourceTest.java`

## 4. 测试用例设计 + 易错规范细节
- 结构：extends SAXSource、is-a javax.xml.transform.Source、2 个公共构造器、systemId 默认 null、setSystemId 回读。
- 真实闭环：ctx 变体与 marshaller 变体都输出完整文档；元素名映射（Person→person、字段→name/age）逐 token 断言；输出以 `<?xml` 开头（锁定）。
- 可重复消费：同一实例第二次 transform 仍成功（锁定——RI 每次 consume 重新 marshal）。
- 负例（实测锁定，**均为 JAXBException 而非 IllegalArgumentException**）：`JAXBSource(ctx, null)` message "Content object can not be null"；`JAXBSource((JAXBContext)null, obj)` 同类。
- 易错点：负例类型勿写 IAE——javadoc 语义与 RI 均为 JAXBException。
