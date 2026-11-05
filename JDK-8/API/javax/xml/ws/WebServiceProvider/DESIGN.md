# javax.xml.ws.WebServiceProvider 测试设计

## 1. API 功能

`javax.xml.ws.WebServiceProvider` 标注于 `javax.xml.ws.Provider` 实现类（`ElementType.TYPE`），四成员：`serviceName` / `portName` / `targetNamespace` / `wsdlLocation`（全部默认空串）。它是"消息级服务"（与 `@WebService` 的"RPC/消息绑定 SEI 级服务"对偶）的端点声明：实现类直接消费/产出整个消息载荷（Source）或整个消息（SOAPMessage/DataSource），配合 `@ServiceMode`（PAYLOAD/MESSAGE）控制粒度。

## 2. 使用方式

```java
@WebServiceProvider(serviceName = "psvc", portName = "pport",
                    targetNamespace = "urn:p", wsdlLocation = "http://x/prov?wsdl")
public static class FullProvider implements Provider<Source> {
    public Source invoke(Source request) { return request; }
}

Endpoint e = Endpoint.create(new FullProvider());       // create 级不解析 WSDL
e.publish("http://localhost:0/bare-prov");              // 无坏 wsdlLocation 才能发布
e.stop();
```
本测试：①注解矩阵（结构/元注解/四成员默认空串）；②全成员/裸标注回读；③运行时通道——create 级（非 null、未发布、implementor 保持、默认 SOAP 1.1 绑定）、publish/stop 生命周期闭环（裸标注与 MESSAGE 模式两类）、坏 wsdlLocation 负例（create 可过、publish 抛 WebServiceException）。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServiceProvider.html、Provider.html、ServiceMode.html
- JSR 224 (JAX-WS 2.2) §5.5（Provider）
- OpenJDK 8u RI（`com.sun.xml.internal.ws.server.provider.ProviderEndpointModel`、`EndpointImpl.getPrimaryWsdl`）
- 本仓库测试：`JDK-8/API/javax/xml/ws/WebServiceProvider/WebServiceProviderTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：成员集合恰 `{wsdlLocation, serviceName, targetNamespace, portName}` 四 String 全默认空串；Target 恰 {TYPE}；其余同族矩阵。
- 回读：四元组精确、裸标注全空串、equals/hashCode/toString、载体类 implements Provider。
- 运行时通道：create 四态（非 null/未发布/implementor/SOAP11 默认绑定）；publish-stop 闭环 ×2（默认 PAYLOAD 的 Provider&lt;Source&gt;、@ServiceMode(MESSAGE) 的 Provider&lt;Source&gt;）；坏 wsdlLocation 负例。

易错规范细节：
1. **Provider 泛型类型合法性**：规范允许 Source / SOAPMessage / DataSource / JAXB（经 JAXBContext）。`Provider<String>` 在 **create 时通过**、**publish 时**才由 ProviderEndpointModel 校验失败（RI 报 "Illeagal combination - Mode.PAYLOAD and Provider<...>"，注意消息里 Illegal 还拼错了）。所以 create-only 断言对非法泛型也通过——负例必须走 publish 或换合法泛型。
2. **wsdlLocation 非空时 publish 必须能加载该 WSDL**（RI 生成 primary WSDL 失败即抛 ServerRtException extends WebServiceException）；故生命周期闭环的载体不带 wsdlLocation（默认空串），负例载体带 `file:///nonexistent/...`。
3. 发布生命周期只用 `http://localhost:0/`（ephemeral 端口）并立即 stop；不绑固定端口、不真联网。
4. `@ServiceMode` 引用枚举须写全限定 `javax.xml.ws.Service.Mode.MESSAGE`（测试类若未 import Service，裸写 `Service.Mode` 会被解析成包名导致编译错）。
5. RI 的 ServerRtException 消息是 "[failed to localize] cannot.load.wsdl(...)"——本地化失败形态，勿断言消息全文，只断异常类型（WebServiceException）。
