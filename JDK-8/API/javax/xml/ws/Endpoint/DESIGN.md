# Endpoint 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.Endpoint 是 JAX-WS 服务端发布抽象（JSR 224）：包装实现对象（@WebService 或 @WebServiceProvider 标注），支持两种容器形态——独立 HTTP 服务器（publish(String 地址)）与 SPI HttpContext（publish(spi.http.HttpContext)）。提供生命周期（isPublished/stop）、绑定与实现器访问、元数据（WSDL Source 列表）、Executor、属性表与 EndpointReference 导出。常量 WSDL_SERVICE/WSDL_PORT 是发布时注入响应上下文的元数据键。

## 2. 使用方式
典型路径：`Endpoint.create(bindingId 或 impl)` → （可选 setMetadata/setExecutor/setProperties/setHandlerChain）→ `publish("http://localhost:0/path")`（端口 0=ephemeral，内核分配）→ `getEndpointReference(W3CEndpointReference.class)` → `stop()`。静态一步形态 `Endpoint.publish(addr, impl)` 返回已发布端点。本测试闭环：@WebService(serviceName="svc",targetNamespace="urn:t") 标注 impl → create/publish/isPublished==true/getEndpointReference 含 localhost ephemeral 地址/stop/二次 stop 幂等，全程回环不外联。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/Endpoint.html）、JSR 224 (JAX-WS 2.2)、OpenJDK 8u RI（com.sun.xml.internal.ws.transport.http.server.EndpointImpl）、本仓库 EndpointTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：抽象类；10 个抽象实例方法（getBinding/getImplementor/publish×2/stop/isPublished/getMetadata/setMetadata/getExecutor/setExecutor/getProperties/setProperties）；静态 create×4 + 静态 publish×2；常量 "javax.xml.ws.wsdl.service"/"javax.xml.ws.wsdl.port"。
- 创建矩阵：create(SOAP11URI, impl) / create(impl)（默认即 SOAP 1.1）/ 带 feature 变参两种；bindingId **null 也被接受**（落默认 SOAP 绑定，实测）；create(null impl) → NPE（RuntimeException 家族记录）。
- **生命周期闭环（P0）**：isPublished false → publish("http://localhost:0/echo") → true → getEndpointReference(W3C) 非 null（XML 含 http://localhost: 与 addressing ns）→ 无参泛型 getEndpointReference() 返回 W3CEndpointReference 实例 → stop() → false → **再 stop() 幂等不抛**；同实例 stop 后再 publish → ISE/WebServiceException 被拒；两次独立 create/publish/stop 循环；静态 publish 一步形态（含 feature 变参）。
- 负例精确异常表（实测锁定）：
  - publish((String) null) → IllegalArgumentException
  - publish((Object) null)（实例方法）→ NullPointerException（与 String 重载不同，注意区分）
  - 静态 publish(null, impl) → IllegalArgumentException
  - publish("ftp://x/y") → IAE，消息 "ftp protocol based address is not supported"
  - publish("https://...") → IAE "https protocol based address is not supported"
  - publish("")/"not-a-url" → IAE "Cannot create URL for this address ..."
  - publish("http://localhost:0") 无路径 → IAE "The address's path should start with /"
  - 未标注 impl publish → IAE，消息 contains "has neither @WebService nor @WebServiceProvider" 且含类名（**create 阶段不抛，publish 才抛**）
  - 未发布时 getEndpointReference（两种形态）→ WebServiceException "Endpoint is not published yet"；stop 后同样。
- setExecutor(null) 不抛（清空后 getExecutor()==null）；默认 executor null。
- getProperties()：默认空 map 非 null；**每次调用返回快照副本**（跨调用 put 不可见，setProperties 提交）；setProperties(map) 回读成功；setProperties(null) → RI 防御拷贝 NPE（实测锁定）。
- getMetadata()：默认 null（RI）；setMetadata(空列表/带 StreamSource 列表) 回读计数。
- getBinding() 是 SOAPBindingImpl：getBindingID()=="http://schemas.xmlsoap.org/wsdl/soap/http"；handler chain 默认空、可 add/setHandlerChain 往返。
- setEndpointContext(自定义/null)：均不抛（JAX-WS 2.2 EndpointContext 关联）。
- 坑：publish 必须用 localhost:0（ephemeral）且立即 stop；同一 Endpoint 不能二次 publish；Path.toUri() 大写 U。
