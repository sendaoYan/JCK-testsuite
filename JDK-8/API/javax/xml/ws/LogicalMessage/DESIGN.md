# LogicalMessage 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.LogicalMessage 是 handler 链中逻辑消息视图（JSR 224 §9.4.1）：从 LogicalMessageContext.getMessage() 取得，双形态访问负载——getPayload()/setPayload(Source)（协议中立 XML 流）与 getPayload(JAXBContext)/setPayload(Object, JAXBContext)（JAXB 对象进出）。handler 修改后随消息继续流转。

## 2. 使用方式
典型路径：LogicalHandler.handleMessage 里 `LogicalMessage msg = context.getMessage(); Source p = msg.getPayload(); ... msg.setPayload(newSource);`。本测试：结构面 4 签名锁定 + 自造 SimpleLogicalMessage 双形态回读 + Transformer 消费 Source 载荷 + Person 的 JAXB round-trip 旁证。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/LogicalMessage.html）、JSR 224 (JAX-WS 2.2) §9、本仓库 LogicalMessageTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：接口、无父接口、恰 4 声明方法：getPayload()→Source、getPayload(JAXBContext)→Object、setPayload(Source)→void、setPayload(Object,JAXBContext)→void（全 abstract/public）。
- Source 形态闭环：同引用 get/set；Transformer 把 getPayload() 结果写到 StreamResult 内容可读（"<r>v9</r>"）；null 载荷 set/get。
- JAXB 形态闭环：setPayload(p,ctx) 存引用+记录 ctx；getPayload(ctx) 同对象回读；Person（@XmlRootElement）marshal 输出 person/name/age 元素映射 + unmarshal 字段全等（ JAXB round-trip 旁证载荷类型）。
- 双形态互不干扰：setPayload(Source) 后 JAXB 形态保持。
- 关联：LogicalMessageContext 接口存在且与 LogicalMessage 是不同类型（getMessage() 桥接）。
- 坑：JAXB 形态返回对象是对载荷的反序列化副本（非缓存引用）；Source 是单次消费流，Transformer 读一次后再读为空。
