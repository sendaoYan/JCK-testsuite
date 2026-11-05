# SOAPConstants — 设计案

## 1. API 功能
SAAJ 常量表（接口）：4 个协议选择器、5 个命名空间 URI、2 个内容类型、4 个 actor/role URI、SOAP_ENV_PREFIX，以及 5 个 1.2 标准 fault QName（VersionMismatch/MustUnderstand/DataEncodingUnknown/Sender/Receiver，ns=1.2 envelope，前缀 env）。

## 2. 使用方式
`MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)` 选协议；URI_NS_* 作命名空间比较基准；fault QName 直接作 setFaultCode 入参；URI_SOAP_ACTOR_NEXT 作 header 过滤器。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：分组常量全等表（协议选择器 4+1 等价断言、ns URI 5、内容类型 2+prefix 1、actor/role 4、fault QName 5），每 group 附行为闭环（选择器驱动工厂产对应 ns；保存后的消息 Content-Type 以 SOAP_1_1_CONTENT_TYPE 开头；NEXT actor 可作过滤器；SENDER fault 常量直接可作 1.2 fault code）。断言 29 条（旧 3）。
- 坑1：DEFAULT_SOAP_PROTOCOL == SOAP_1_1_PROTOCOL（同值 "SOAP 1.1 Protocol"）。
- 坑2：SOAP_1_1_CONTENT_TYPE 是 "text/xml"（不含 charset）；实际消息头是 "text/xml; charset=UTF-8"——断言用 startsWith。
- 坑3：fault QName 的 ns 都是 **1.2** envelope（包括在 1.1 语境常见错用的 Sender/Receiver），前缀统一 "env"。
