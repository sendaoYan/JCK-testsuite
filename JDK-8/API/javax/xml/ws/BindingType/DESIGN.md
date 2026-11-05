# javax.xml.ws.BindingType 测试设计

## 1. API 功能

`javax.xml.ws.BindingType` 标注于服务实现类（`ElementType.TYPE`），声明该端点使用的绑定协议 URI（`value()` 单成员）。JAX-WS 运行时在 `Endpoint.create(impl)` 时读取该注解决定 `Binding.getBindingID()`，典型取值为 `SOAPBinding.SOAP11HTTP_BINDING`、`SOAPBinding.SOAP12HTTP_BINDING`、`HTTPBinding.HTTP_BINDING`。它是注解驱动的「实现类 → 绑定」通道，与 `Endpoint.create(String bindingId, impl)` 显式参数通道互为补充。

## 2. 使用方式

```java
@WebService(serviceName = "httpSvc", targetNamespace = "urn:t")
@BindingType(HTTPBinding.HTTP_BINDING)
public static class HttpImpl { public String echo(String s) { return s; } }

Endpoint e = Endpoint.create(new HttpImpl());
e.getBinding().getBindingID();  // == HTTPBinding.HTTP_BINDING
```
本测试走「HTTP / SOAP11 / SOAP12 / 裸标注 / 未标注」五形态矩阵：前三种经 `Endpoint.create` 回读 bindingID；裸标注与未标注均回落默认 SOAP 1.1/HTTP；另验证显式 `create(bindingId, impl)` 参数**覆盖**注解；SOAP 1.2 形态做 localhost:0 发布-停止生命周期闭环。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/BindingType.html
- JSR 224 (JAX-WS 2.2) 第 5 章Bindings
- OpenJDK 8u RI（`com.sun.xml.internal.ws.server.EndpointFactory` 读取 @BindingType）
- 本仓库测试：`JDK-8/API/javax/xml/ws/BindingType/BindingTypeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解面：与注解族通用矩阵相同（RUNTIME、Target 恰 {TYPE}、Documented、非 Inherited 非 Repeatable、声明元注解恰 3 个、单成员 value public abstract 返回 String）。
- 默认值：`value()` 默认 **空字符串**（见易错 1）。
- 常量锁定：SOAP11HTTP_BINDING `http://schemas.xmlsoap.org/wsdl/soap/http`、SOAP12HTTP_BINDING `http://www.w3.org/2003/05/soap/bindings/HTTP/`、HTTP_BINDING `http://www.w3.org/2004/08/wsdl/http`（注意是 **2004/08** 不是 2004/06）。
- 回读：四种标注形态 value 精确回读；equals/hashCode/toString 通用契约；未标注类 null。
- 运行时通道：create 后 bindingID 与注解一致；裸/未标注回落 SOAP11；显式 bindingId 参数优先于注解；SOAP12 publish("http://localhost:0/...") + stop 闭环。

易错规范细节：
1. **`value()` 默认值是 `""`，不是 `SOAPBinding.SOAP11HTTP_BINDING` 常量**——javadoc 文字说"未指定时用默认 SOAP 1.1/HTTP 绑定"，指的是运行时语义（空串触发回落），注解源码 default 即 `""`。断言 `getDefaultValue()` 必须用空串。
2. **HTTP_BINDING 端点不能 publish**：RI 报 `Cannot generate WSDL for binding`（ServerRtException extends WebServiceException）；HTTP 绑定通道只做 create 级断言，勿写 publish 闭环。
3. HTTP_BINDING URI 极易记错（2004/06/2004/08 之争），必须实测锁定。
4. 显式 `Endpoint.create(bindingId, impl)` 的 bindingId 参数会覆盖注解（实测参数优先），可作为覆盖性断言。
5. publish 生命周期只用 `http://localhost:0/`（ephemeral 端口）并立即 stop；不绑固定端口。
