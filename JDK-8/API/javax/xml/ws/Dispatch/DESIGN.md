# Dispatch 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.Dispatch<T> 是消息级（非 SEI 代理）客户端接口（JSR 224 §5.4）：继承 BindingProvider，提供四种调用形态——invoke(T) 同步、invokeAsync(T)→Response<T> 轮询式异步、invokeAsync(T,AsyncHandler)→Future<?> 回调式异步、invokeOneWay(T) 单向。类型参数 T 的合法支撑类型：javax.xml.transform.Source（及子类）、javax.xml.soap.SOAPMessage（需 SOAP 绑定+MESSAGE 模式）、按 JAXBContext 构造时的 Object。

## 2. 使用方式
典型路径：`Dispatch<Source> d = service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD); d.getRequestContext().put(...); Source resp = d.invoke(payload);`。本测试：WSDL 服务与 addPort 服务双路径创建 Dispatch（Source-PAYLOAD/MESSAGE、SOAPMessage-MESSAGE、JAXBContext、feature 变参），只做对象层断言，**绝不 invoke**（soap:address 指向不监听端口）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/Dispatch.html）、JSR 224 (JAX-WS 2.2)、OpenJDK 8u RI（com.sun.xml.internal.ws.client.dispatch）、本仓库 DispatchTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：接口、单父接口 BindingProvider、泛型 <T>、恰 4 个自身声明方法。
- 签名精确锁定：invoke(Object)→Object（擦除）、invokeAsync(Object)→Response、invokeAsync(Object,AsyncHandler)→Future<?>、invokeOneWay(Object)→void；全部 public abstract。
- 创建矩阵：WSDL 服务的 createDispatch(portQn, Source, PAYLOAD/MESSAGE)、(portQn, SOAPMessage, MESSAGE)、(portQn, JAXBContext, PAYLOAD)→Dispatch<Object>、空 feature 变参；addPort（无 WSDL）服务的 Dispatch 同样可用且端点地址预填。
- 负例：未知端口 → WebServiceException（消息含 ghost 端口名）。
- 自造记录型 Dispatch：四形态调用序列记录（invoke→async→async+handler→oneway 顺序）、invokeAsync 返回的 Response.get()/isDone/isCancelled/getContext 语义（FutureTask 需 run() 预完成，否则 get() 永久阻塞——坑）。
- 坑：测试中 FutureTask 必须显式 run() 才能非阻塞断言；invokeAsync 对真实 Dispatch 调用会真发起网络连接（地址不可达时异步失败），因此对象层只创建不调用。
