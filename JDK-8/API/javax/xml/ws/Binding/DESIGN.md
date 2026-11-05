# Binding 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.Binding 是绑定抽象（JSR 224 §4.1）：暴露 handler 链（getHandlerChain/setHandlerChain——代理与 Dispatch 客户端、Endpoint 服务端共用的处理器链管理面）与绑定标识（getBindingID——SOAP 1.1/SOAP 1.2 HTTP 或 XML/HTTP URI）。子接口 soap.SOAPBinding 附加 roles/setMTOMEnabled，http.HTTPBinding 为纯 XML 绑定标记。

## 2. 使用方式
典型路径：`List<Handler> chain = binding.getHandlerChain(); chain.add(myHandler); binding.setHandlerChain(chain);`。本测试：结构面 3 方法 + 真实 Binding（Endpoint.create(...).getBinding()，RI SOAPBindingImpl）的链往返、绑定 ID 常量对偶、HTTP 绑定形态。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/Binding.html）、JSR 224 (JAX-WS 2.2)、OpenJDK 8u RI（com.sun.xml.internal.ws.binding.BindingImpl）、本仓库 BindingTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：接口、无父接口、恰 3 声明方法（getHandlerChain→List<Handler>、setHandlerChain(List)→void、getBindingID→String）。
- 绑定 ID 契约：SOAP11 "http://schemas.xmlsoap.org/wsdl/soap/http"（== soap.SOAPBinding.SOAP11HTTP_BINDING）、SOAP12 "http://www.w3.org/2003/05/soap/bindings/HTTP/"、HTTP "http://www.w3.org/2004/08/wsdl/http"（== http.HTTPBinding.HTTP_BINDING）。
- 真实 Binding 闭环（P0）：Endpoint.create(SOAP11URI).getBinding() instanceof soap.SOAPBinding；默认链空非 null；构造 2 处理器链 setHandlerChain → get 回读 size==2 且两处理器在场（RI set 存储/回读以内容为准——同实例或防御拷贝均记录）。
- **RI 快照语义（坑）**：getHandlerChain() 返回可变快照副本——外部 add 对下一次 get 不可见（实测锁定）；需持久修改必须走 setHandlerChain。
- setHandlerChain(空链) 合法；setHandlerChain(null) RI 接受（记录不抛 Error）；两个 Endpoint 链独立。
- HTTP 绑定：create(HTTP_BINDING_URI) 成功且 getBindingID 对偶。
