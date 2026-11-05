# javax.xml.ws.soap.SOAPFaultException — 设计文档

## 一、API 功能
`SOAPFaultException` 扩展 ProtocolException，唯一公共构造器 `SOAPFaultException(SOAPFault fault)` + 自有方法 `getFault()` 返回同一 SAAJ SOAPFault 实例。它把 SOAP 协议故障作为异常在客户端/服务端传播：fault 对象携带 code/reason/detail/actor。

## 二、使用方式
服务端 SEI 方法抛出 → 运行时封成 SOAP fault 下发；客户端运行时收 fault → 解成 SOAPFaultException 抛给业务。getFault() 后可直接读 FaultCode QName/FaultString/Detail。测试覆盖层级、fault 同一实例与可变性、null 构造负例、回环通道的服务端故障传播。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.soap.SOAPFaultException
- JSR 224 §5.2 SOAP Fault Mapping
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.fault.SOAPFaultException
- 本仓库测试：JDK-8/API/javax/xml/ws/soap/SOAPFaultException/SOAPFaultExceptionTest.java

## 四、测试用例设计 + 易错规范细节
- fault 同一实例：getFault()==构造传入实例，重复调用稳定；构造后修改 fault（setFaultString/addDetail/setFaultActor）对异常可见。
- code/reason 往返：addFault(QName,reason,Locale) 后 getFaultCodeAsQName() 局部名+命名空间、getFaultString() 精确回读；默认 addFault() code 缺省（Server）、message 由 fault 派生非 null。
- 负例：new SOAPFaultException(null) → NullPointerException（实测）。
- 真实通道：Provider 抛 RuntimeException("boom") → 客户端 SOAPFaultException，faultstring 含 "boom"，code 存在，getMessage() 非 null。
- 易错点：getCause 默认 null（fault 即载荷，不走 cause 链）；SAAJ 构造 MessageFactory.newInstance().createMessage().getSOAPBody().addFault(...) 需处理 SOAPException。
