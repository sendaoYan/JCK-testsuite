# Service 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.Service 是 JAX-WS 客户端入口工厂（JSR 224）：从 WSDL 文档（或纯 QName 无 WSDL 形态）构建服务视图，发现端口（getPorts）、生成 SEI 动态代理（getPort）、创建消息级 Dispatch、管理 HandlerResolver 与 Executor。无 public 构造器，只能经静态 create 工厂族引导。嵌套枚举 Service.Mode 定义 Dispatch 的两种消息粒度（MESSAGE=整条 SOAP 消息 / PAYLOAD=仅消息负载）。

## 2. 使用方式
典型路径：`Service.create(wsdlUrl, new QName(ns, "svcName"))` → `svc.getPort(portQName, Echo.class)` 得代理（代理同时是 BindingProvider，请求上下文预填 soap:address）。无 WSDL 形态：`Service.create(qn)` → `addPort(portQn, bindingId, endpointAddr)` → `createDispatch(...)`。本测试的闭环：测试内写最小 document/literal wrapped WSDL 到临时文件 → `f.toUri().toURL()`（注意 Path 上是大写 U 的 toUri）→ create → 服务名/端口迭代/SEI 代理/Dispatch 全链路断言（绝不发起网络调用，soap:address 指向不监听的本机端口仅作元数据）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/Service.html）、JSR 224 (JAX-WS 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.ws.client.WSServiceDelegate）、本仓库 ServiceTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：具体类、无 public 构造、静态 create 四形态（(URL,QName)/(URL,QName,features)/(QName)/(QName,features)）。
- **WSDL 服务发现闭环（P0）**：getServiceName=={urn:t}svc、getWSDLDocumentLocation 回读同 URL、getPorts 恰含 {urn:t}EchoPort、getPort(portQn,SEI) 与 getPort(SEI) 双形态代理 instanceof SEI、代理为 JDK 动态代理。
- SEI 契约：**必须** @WebService(name="EchoPort", targetNamespace="urn:t")（javax.jws）；裸 SEI（未标注）→ RuntimeModelerException（消息含 "WebService annotation"）；name 不匹配 WSDL portType → WebServiceException "Undefined port type"。
- 请求上下文：代理的 requestContext 已含 ENDPOINT_ADDRESS_PROPERTY==WSDL soap:address；soapaction.use/uri 键预置但值 null。
- 负例矩阵（实测锁定）：
  - create(url, null) → WebServiceException "null is not a valid service"
  - create(url, 错服务名) → WebServiceException 消息含有效服务清单（"Valid services are: {urn:t}svc"）
  - getPort(未知端口) → WebServiceException 消息含有效端口清单
  - createDispatch(未知端口) → WebServiceException
  - **create(null, qn) 不抛**：空服务 getWSDLDocumentLocation()==null、getPorts() 空迭代（勿按直觉断言抛异常）
  - 垃圾 WSDL → RuntimeException（RI 为 XMLStreamReaderException，勿锁实现名；消息非空）
  - 不存在的 WSDL 文件 URL → RuntimeException
- HandlerResolver：默认 **null**（RI 实测，非空对象）；setHandlerResolver(r) 回读同一实例；(null) 接受。
- Executor：默认 null；setExecutor 同一实例回读；(null) 接受。
- addPort：动态注册端口后 getPorts 可发现、createDispatch 可用（binding+address 由 addPort 提供，无需 WSDL）。
- Dispatch 形态：Source+PAYLOAD/MESSAGE、SOAPMessage+MESSAGE、JAXBContext（Dispatch<Object>）、空 feature 变参。
- Service.Mode：恰 2 常量 [MESSAGE, PAYLOAD]、ordinal 0/1、valueOf 大小写敏感、compareTo 语义。
- 坑：getPorts() 的 Iterator 在未 next 前 remove() 抛 IllegalStateException；两个迭代器独立。
