# BindingProvider 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.BindingProvider 是 SEI 代理与 Dispatch 的公共基接口（JSR 224）：定义消息上下文对——getRequestContext()（调用前可配置：端点地址/HTTP 基本认证/会话维持/SOAPAction）与 getResponseContext()（调用后只读响应元数据），以及 getBinding() 与两个 getEndpointReference 形态。六个标准属性常量以 "javax.xml.ws." 前缀字符串键暴露。

## 2. 使用方式
典型路径：`Echo proxy = svc.getPort(portQn, Echo.class); BindingProvider bp = (BindingProvider) proxy; bp.getRequestContext().put(ENDPOINT_ADDRESS_PROPERTY, newUrl);`。本测试闭环：内嵌最小 WSDL 生成真实代理 → 请求上下文断言 soap:address 预填 → 6 属性 put/get 回读 → Dispatch 侧同一契约面。不发起任何调用。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/BindingProvider.html）、JSR 224 (JAX-WS 2.2)、OpenJDK 8u RI（com.sun.xml.internal.ws.client.BindingProviderProperties）、本仓库 BindingProviderTest。

## 4. 测试用例设计 + 易错规范细节
- **6 常量串全等（锁定契约值）**：USERNAME "javax.xml.ws.security.auth.username"、PASSWORD "...auth.password"、ENDPOINT_ADDRESS "javax.xml.ws.service.endpoint.address"、SESSION_MAINTAIN "javax.xml.ws.session.maintain"、SOAPACTION_USE "javax.xml.ws.soap.http.soapaction.use"、SOAPACTION_URI "...soapaction.uri"；互异 6 串；public static final String。
- 结构面：接口、无父接口、恰 5 方法（getRequestContext/getResponseContext/getBinding/getEndpointReference×2）；上下文返回 Map<String,Object>；typed getEndpointReference 泛型 <T extends EndpointReference>。
- 代理上下文闭环（P0）：WSDL 代理的 requestContext **已预填** ENDPOINT_ADDRESS_PROPERTY==WSDL soap:address；soapaction.use/uri 键存在但值 null；put 跨 getRequestContext() 调用可见（同一可变视图）；自定义键与非 String 值可存取；remove 生效。
- **getResponseContext() 在首次 invoke 前为 null（RI 惰性创建，实测）**——javadoc 未规定预填充，测试锁 RI 行为并写宽容分支。
- Dispatch 同为 BindingProvider：createDispatch(...).getRequestContext() put/get 回读。
- getBinding()：代理侧返回 SOAP 1.1 binding（id "http://schemas.xmlsoap.org/wsdl/soap/http"），handler chain 默认空列表。
- 坑：不要断言响应上下文非 null；不要在请求上下文 put 后断言 getResponseContext 变化（两者独立）。
