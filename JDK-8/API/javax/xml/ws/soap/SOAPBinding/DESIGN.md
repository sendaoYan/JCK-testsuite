# javax.xml.ws.soap.SOAPBinding — 设计文档

## 一、API 功能
`SOAPBinding` 扩展 Binding，SOAP 绑定接口：4 个常量——SOAP11HTTP_BINDING、SOAP12HTTP_BINDING（注意尾部斜杠 "http://www.w3.org/2003/05/soap/bindings/HTTP/"）、SOAP11HTTP_MTOM_BINDING、SOAP12HTTP_MTOM_BINDING（各加 "?mtom=true"）——及自有方法 setRoles/getRoles（SOAP 角色链）。继承 getBindingID/getHandlerChain/setHandlerChain。

## 二、使用方式
addPort/getBinding 强转后 setRoles 定义本端点承担的角色；角色集默认含 "next"（SOAP 1.1）/ "ultimateReceiver" 语义。空集被规范化为 [next]；非 URI 字符串抛 UnsupportedOperationException。测试含常量关系表与真实 Endpoint.create 的绑定活体断言。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.soap.SOAPBinding
- JSR 224 §10 SOAP Bindings；SOAP 1.1/1.2
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.binding.SOAPBindingImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/soap/SOAPBinding/SOAPBindingTest.java

## 四、测试用例设计 + 易错规范细节
- 常量表：4 个 URI 逐字全等；MTOM 变体 = 基 URI + "?mtom=true"；SOAP12 基 URI 尾部有 "/"；4 者两两互异；与 HTTP_BINDING 互异。
- 活体：Endpoint.create(...).getBinding() instanceof SOAPBinding；MTOM 开关可切（isMTOMEnabled/setMTOMEnabled 往返）；MessageFactory/SOAPFactory 可取且非 null；默认 roles 含 next；自定义 roles 往返；setRoles(空) → [next]；setRoles(null) 容忍；非 URI（如 "not a uri"）→ UnsupportedOperationException（实测非 IAE）。
- 易错点：SOAP12HTTP_BINDING 的尾斜杠与 MTOM 变体拼接规则是最易写错的两个字符串；setRoles 后 getRoles 返回副本/规范化形式，勿断言同一 List 实例。
